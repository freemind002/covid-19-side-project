package com.example.covid_19.dto;

import java.time.LocalDate;

public class DeathsSummaryDto {
    private final Long totalDailySum;
    private final LocalDate starDate;
    private final LocalDate endDate;

    public DeathsSummaryDto(Long totalDailySum, LocalDate starDate, LocalDate endDate) {
        this.totalDailySum = totalDailySum;
        this.starDate = starDate;
        this.endDate = endDate;
    }

    public Long getTotalDailySum() {
        return totalDailySum;
    }

    public LocalDate getStarDate() {
        return starDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
