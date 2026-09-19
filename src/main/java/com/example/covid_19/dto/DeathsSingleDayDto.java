package com.example.covid_19.dto;

import java.time.LocalDate;

public class DeathsSingleDayDto {
    private final Long dailySum;
    private final LocalDate targetDate;

    public DeathsSingleDayDto(Long dailySum, LocalDate targetDate) {
        this.dailySum = dailySum;
        this.targetDate = targetDate;
    }

    public Long getDailySum() {
        return dailySum;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }
}
