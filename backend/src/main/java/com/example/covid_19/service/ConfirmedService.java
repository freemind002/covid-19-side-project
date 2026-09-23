package com.example.covid_19.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.example.covid_19.dto.ConfirmedResponseDto;
import com.example.covid_19.dto.ConfirmedSingleDayDto;
import com.example.covid_19.dto.ConfirmedSummaryDto;
import com.example.covid_19.dto.TableRecordDto;
import com.example.covid_19.model.Confirmed;

@Service
public class ConfirmedService {
    // 💡 兩個都宣告為 final，確保安全與不可變性
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ConfirmedRepository confirmedRepository;

    // 💡 統一在同一個建構子內進行注入（只有一個建構子時，@Autowired 可以省略）
    public ConfirmedService(NamedParameterJdbcTemplate jdbcTemplate, ConfirmedRepository confirmedRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.confirmedRepository = confirmedRepository;
    }

    public List<ConfirmedResponseDto> getAllConfirmedData() {
        // 相當於 SELECT * FROM confirmed
        List<Confirmed> confirmedList = confirmedRepository.findAll();

        // 將 Model 轉換成 DTO
        return confirmedList.stream()
                .map(c -> new ConfirmedResponseDto(
                        c.getUpdatedOn(),
                        c.getDaily(),
                        c.getCumulative(),
                        c.getGeographyId()))
                .collect(Collectors.toList());
    }

    public ConfirmedSummaryDto getSummary(LocalDate startDate, LocalDate endDate, List<Long> geographyIds) {
        // 1. 防呆驗證：如果日期有填，且開始日期大於結束日期，直接擋下！
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "開始日期不能大於結束日期！");
        }
        // 如果前端沒有傳入 geographyIds 或者是空的，傳 null 讓 JPQL 的 :geographyIds IS NULL 生效
        List<Long> targetIds = (geographyIds == null || geographyIds.isEmpty()) ? null : geographyIds;

        return confirmedRepository.getConfirmedSummary(startDate, endDate, targetIds);
    }

    public ConfirmedSingleDayDto getDailySummaryByDate(LocalDate targetDate, List<Long> geographyIds) {
        if (targetDate == null) {
            throw new IllegalArgumentException("必須指定查詢日期！");
        }

        List<Long> targetIds = (geographyIds == null || geographyIds.isEmpty()) ? null : geographyIds;

        return confirmedRepository.getConfirmedByDate(targetDate, targetIds);
    }

    public Page<TableRecordDto> getTablePage(
            LocalDate startDate, LocalDate endDate, List<Long> geographyIds,
            int page, int size, List<String> sortParams) {
        // 防呆驗證：如果日期有填，且開始日期大於結束日期，直接擋下！
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "開始日期不能大於結束日期！");
        }

        // 1. 判斷是否有給地區
        int hasGeographyIds = (geographyIds == null || geographyIds.isEmpty()) ? 0 : 1;
        List<Long> targetIds = (hasGeographyIds == 0) ? List.of(-1L) : geographyIds;

        // 2. 建立共用的參數來源
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("startDate", startDate);
        params.addValue("endDate", endDate);
        params.addValue("hasGeographyIds", hasGeographyIds);
        params.addValue("geographyIds", targetIds);

        // 3. 先計算總筆數 (Count Query) 以得知總頁數
        String countSql = "SELECT COUNT(*) FROM confirmed c " +
                "WHERE c.updated_on BETWEEN :startDate AND :endDate " +
                "AND (:hasGeographyIds = 0 OR c.geography_id IN (:geographyIds))";
        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        if (total == null)
            total = 0L;

        // 💡 4. 計算總頁數並檢查 page 是否合法
        // 數學公式：ceil(total / size)
        long totalPages = (total == 0) ? 0 : (long) Math.ceil((double) total / size);

        // 如果資料庫有資料，但傳入的 page 已經大於或等於總頁數 (代表超過範圍)
        // 或者資料庫完全沒資料但傳入的 page > 0
        if (total > 0 && page >= totalPages) {
            throw new IllegalArgumentException("請求的頁碼 (page: " + page + ") 超出範圍，最大有效頁碼為 " + (totalPages - 1));
        } else if (total == 0 && page > 0) {
            throw new IllegalArgumentException("目前查無資料，頁碼只能為 0");
        }

        // 5. 修正後的排序解析邏輯（支援 [field, desc] 或 [field,asc] 的陣列結構）
        List<String> orderClauses = new ArrayList<>();
        if (sortParams == null || sortParams.isEmpty()) {
            orderClauses.add("c.updated_on ASC");
            orderClauses.add("c.geography_id ASC");
        } else {
            // 因為參數可能是 ["updatedOn", "desc"] 這種成對的結構
            for (int i = 0; i < sortParams.size(); i++) {
                String field = sortParams.get(i).trim();
                String direction = "ASC"; // 預設升冪

                // 如果下一個元素剛好是 asc 或 desc，把它當作方向吃掉
                if (i + 1 < sortParams.size()) {
                    String next = sortParams.get(i + 1).trim();
                    if ("asc".equalsIgnoreCase(next) || "desc".equalsIgnoreCase(next)) {
                        direction = next.toUpperCase();
                        i++; // 略過下一個元素，因為已經被當作方向使用了解析
                    }
                } else if (field.contains(",")) {
                    // 也有可能有些情況是 "updatedOn,desc" 黏在同一個字串裡，這裡也做個防呆
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

        // 6. 查詢分頁資料 (Data Query)
        String dataSql = "SELECT " +
                "c.updated_on AS updatedOn, " +
                "c.daily AS confirmedDaily, " +
                "SUM(c.daily) OVER (PARTITION BY c.geography_id ORDER BY c.updated_on) AS confirmedCumulative, " +
                "d.daily AS deathsDaily, " +
                "SUM(d.daily) OVER (PARTITION BY d.geography_id ORDER BY c.updated_on) AS deathsCumulative, " +
                "g.country_region AS countryRegion, " +
                "g.province_state AS provinceState " +
                "FROM confirmed c " +
                "LEFT JOIN deaths d ON c.geography_id = d.geography_id AND c.updated_on = d.updated_on " +
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

        // 7. 包裝成 Spring 的 Page 返回
        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(records, pageable, total);
    }
}