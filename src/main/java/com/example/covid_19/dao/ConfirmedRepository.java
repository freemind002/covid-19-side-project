package com.example.covid_19.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.covid_19.dto.ConfirmedSingleDayDto;
import com.example.covid_19.dto.ConfirmedSummaryDto;
import com.example.covid_19.dto.TableRecordDto;
import com.example.covid_19.model.Confirmed;
import com.example.covid_19.model.ConfirmedId;

public interface ConfirmedRepository extends JpaRepository<Confirmed, ConfirmedId> {
        @Query("SELECT new com.example.covid_19.dto.ConfirmedSummaryDto(" +
                        "SUM(c.daily), :startDate, :endDate) " +
                        "FROM Confirmed c " +
                        "WHERE c.updatedOn BETWEEN :startDate AND :endDate " +
                        "AND (:geographyIds IS NULL OR c.geographyId IN :geographyIds)")
        ConfirmedSummaryDto getConfirmedSummary(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("geographyIds") List<Long> geographyIds);

        // 新增：查詢「特定單日」的 daily 總和（支援無地區或多地區篩選）
        @Query("SELECT new com.example.covid_19.dto.ConfirmedSingleDayDto(" +
                        "SUM(c.daily), :targetDate) " +
                        "FROM Confirmed c " +
                        "WHERE c.updatedOn = :targetDate " +
                        "AND (:geographyIds IS NULL OR c.geographyId IN :geographyIds)")
        ConfirmedSingleDayDto getConfirmedByDate(
                        @Param("targetDate") LocalDate targetDate,
                        @Param("geographyIds") List<Long> geographyIds);

        @Query(value = "SELECT " +
                        "c.updated_on AS updatedOn, " +
                        "c.daily AS confirmedDaily, " +
                        "SUM(c.daily) OVER (PARTITION BY c.geography_id ORDER BY c.updated_on) AS confirmedCumulative, "
                        +
                        "d.daily AS deathsDaily, " +
                        "SUM(d.daily) OVER (PARTITION BY d.geography_id ORDER BY c.updated_on) AS deathsCumulative, " +
                        "g.country_region AS countryRegion, " +
                        "g.province_state AS provinceState " +
                        "FROM confirmed c " +
                        "LEFT JOIN deaths d ON c.geography_id = d.geography_id AND c.updated_on = d.updated_on " +
                        "LEFT JOIN geographics g ON c.geography_id = g.id " +
                        "WHERE c.updated_on BETWEEN :startDate AND :endDate " +
                        "AND (:hasGeographyIds = 0 OR c.geography_id IN (:geographyIds))", countQuery = "SELECT COUNT(*) FROM confirmed c WHERE c.updated_on BETWEEN :startDate AND :endDate "
                                        +
                                        "AND (:hasGeographyIds = 0 OR c.geography_id IN (:geographyIds))", nativeQuery = true)
        Page<TableRecordDto> getTableData(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("hasGeographyIds") int hasGeographyIds,
                        @Param("geographyIds") List<Long> geographyIds,
                        Pageable pageable);
}