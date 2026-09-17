package com.example.covid_19.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.covid_19.dto.ConfirmedSummaryDto;
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
}