package com.example.covid_19.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.covid_19.dto.DeathsSingleDayDto;
import com.example.covid_19.dto.DeathsSummaryDto;
import com.example.covid_19.model.Deaths;
import com.example.covid_19.model.DeathsId;

public interface DeathsRepository extends JpaRepository<Deaths, DeathsId> {
    @Query("SELECT new com.example.covid_19.dto.DeathsSummaryDto(" +
            "SUM(d.daily), :startDate, :endDate) " +
            "FROM Deaths d " +
            "WHERE d.updatedOn BETWEEN :startDate AND :endDate " +
            "AND (:geographyIds IS NULL OR d.geographyId IN :geographyIds)")
    DeathsSummaryDto getDeathsSummary(
            @Param("startDate") LocalDate starDate,
            @Param("endDate") LocalDate endDate,
            @Param("geographyIds") List<Long> geographyIds

    );

    @Query("SELECT new com.example.covid_19.dto.DeathsSingleDayDto(" +
            "SUM(d.daily), :targetDate) " +
            "FROM Deaths d " +
            "WHERE d.updatedOn = :targetDate " +
            "AND (:geographyIds IS NULL OR d.geographyId IN :geographyIds)")
    DeathsSingleDayDto getDeathsByDate(
            @Param("targetDate") LocalDate targetDate,
            @Param("geographyIds") List<Long> geographyIds);

}
