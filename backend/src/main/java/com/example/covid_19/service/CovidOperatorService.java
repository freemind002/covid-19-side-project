package com.example.covid_19.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.covid_19.dao.ConfirmedRepository;
import com.example.covid_19.dao.DeathsRepository;
import com.example.covid_19.dto.TableRecordDto;

@Service
public class CovidOperatorService {

        private final NamedParameterJdbcTemplate jdbcTemplate;
        private final ConfirmedRepository confirmedRepository;
        private final DeathsRepository deathsRepository;

        // 統一透過建構子注入
        public CovidOperatorService(
                        NamedParameterJdbcTemplate jdbcTemplate,
                        ConfirmedRepository confirmedRepository,
                        DeathsRepository deathsRepository) {
                this.jdbcTemplate = jdbcTemplate;
                this.confirmedRepository = confirmedRepository;
                this.deathsRepository = deathsRepository;
        }

        /**
         * 專供 Operator 使用的彈性分頁、排序與動態累計查詢
         */
        public Page<TableRecordDto> getOperatorTablePage(
                        LocalDate startDate, LocalDate endDate, List<Long> geographyIds,
                        int page, int size, List<String> sortParams) {

                // 1. 防呆驗證：如果日期有填，且開始日期大於結束日期，直接擋下！
                if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "開始日期不能大於結束日期！");
                }

                // 2. 判斷是否有給地區
                int hasGeographyIds = (geographyIds == null || geographyIds.isEmpty()) ? 0 : 1;
                List<Long> targetIds = (hasGeographyIds == 0) ? List.of(-1L) : geographyIds;

                // 3. 建立共用的參數來源
                MapSqlParameterSource params = new MapSqlParameterSource();
                params.addValue("startDate", startDate);
                params.addValue("endDate", endDate);
                params.addValue("hasGeographyIds", hasGeographyIds);
                params.addValue("geographyIds", targetIds);

                // 4. 先計算總筆數 (Count Query) 以得知總頁數（維持與 dataSql 相同的 JOIN 與 WHERE 結構）
                String countSql = "SELECT COUNT(*) FROM confirmed c " +
                                "LEFT JOIN deaths d ON c.geography_id = d.geography_id AND c.updated_on = d.updated_on "
                                +
                                "LEFT JOIN geographics g ON c.geography_id = g.id " +
                                "WHERE c.updated_on BETWEEN :startDate AND :endDate " +
                                "AND (:hasGeographyIds = 0 OR c.geography_id IN (:geographyIds))";

                Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
                if (total == null)
                        total = 0L;

                System.out.println("DEBUG ---> startDate: " + startDate + ", endDate: " + endDate + ", total count: "
                                + total + ", totalPages: " + (long) Math.ceil((double) total / size));

                // 5. 計算總頁數並檢查 page 是否合法
                long totalPages = (total == 0) ? 0 : (long) Math.ceil((double) total / size);

                if (total > 0 && page >= totalPages) {
                        throw new IllegalArgumentException(
                                        "請求的頁碼 (page: " + page + ") 超出範圍，最大有效頁碼為 " + (totalPages - 1));
                } else if (total == 0 && page > 0) {
                        throw new IllegalArgumentException("目前查無資料，頁碼只能為 0");
                }

                // 6. 排序解析邏輯（支援多欄位與方向解析）
                List<String> orderClauses = new ArrayList<>();
                if (sortParams == null || sortParams.isEmpty()) {
                        orderClauses.add("c.updated_on ASC");
                        orderClauses.add("c.geography_id ASC");
                } else {
                        for (int i = 0; i < sortParams.size(); i++) {
                                String field = sortParams.get(i).trim();
                                String direction = "ASC";

                                if (i + 1 < sortParams.size()) {
                                        String next = sortParams.get(i + 1).trim();
                                        if ("asc".equalsIgnoreCase(next) || "desc".equalsIgnoreCase(next)) {
                                                direction = next.toUpperCase();
                                                i++;
                                        }
                                } else if (field.contains(",")) {
                                        String[] parts = field.split(",");
                                        field = parts[0].trim();
                                        if (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())) {
                                                direction = "DESC";
                                        }
                                }

                                String dbColumn = switch (field) {
                                        case "updated_on", "updatedOn" -> "c.updated_on";
                                        case "geography_id", "geographyId" -> "c.geography_id";
                                        case "confirmed_daily", "confirmedDaily" -> "c.daily";
                                        case "deaths_daily", "deathsDaily" -> "d.daily";
                                        case "country_region", "countryRegion" -> "g.country_region";
                                        case "province_state", "provinceState" -> "g.province_state";
                                        default -> null;
                                };

                                if (dbColumn != null) {
                                        orderClauses.add(dbColumn + " " + direction);
                                }
                        }
                }

                if (orderClauses.isEmpty()) {
                        orderClauses.add("c.updated_on ASC");
                }

                String orderBySql = " ORDER BY " + String.join(", ", orderClauses);

                // 7. 查詢分頁資料 (Data Query 搭配視窗函數計算累積數)
                String dataSql = "SELECT " +
                                "c.updated_on AS updatedOn, " +
                                "c.daily AS confirmedDaily, " +
                                "SUM(c.daily) OVER (PARTITION BY c.geography_id ORDER BY c.updated_on) AS confirmedCumulative, "
                                +
                                "d.daily AS deathsDaily, " +
                                "SUM(d.daily) OVER (PARTITION BY d.geography_id ORDER BY c.updated_on) AS deathsCumulative, "
                                +
                                "g.country_region AS countryRegion, " +
                                "g.province_state AS provinceState " +
                                "FROM confirmed c " +
                                "LEFT JOIN deaths d ON c.geography_id = d.geography_id AND c.updated_on = d.updated_on "
                                +
                                "LEFT JOIN geographics g ON c.geography_id = g.id " +
                                "WHERE c.updated_on BETWEEN :startDate AND :endDate " +
                                "AND (:hasGeographyIds = 0 OR c.geography_id IN (:geographyIds)) " +
                                orderBySql + " LIMIT :limit OFFSET :offset";

                params.addValue("limit", size);
                params.addValue("offset", (long) page * size);

                List<TableRecordDto> records = jdbcTemplate.query(
                                dataSql,
                                params,
                                new BeanPropertyRowMapper<>(TableRecordDto.class));

                // 8. 包裝成 Spring 的 Page 返回
                Pageable pageable = PageRequest.of(page, size);
                return new PageImpl<>(records, pageable, total);
        }
}