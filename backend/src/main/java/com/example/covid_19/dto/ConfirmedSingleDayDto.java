package com.example.covid_19.dto;

import java.time.LocalDate;

public class ConfirmedSingleDayDto {
    private final Long dailySum; // 該指定日期的 daily 總和
    private final LocalDate targetDate; // 查詢的特定日期

    public ConfirmedSingleDayDto(Long dailySum, LocalDate targetDate) {
        this.dailySum = dailySum;
        this.targetDate = targetDate;
    }

    // Getters
    public Long getDailySum() {
        return dailySum;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }
}
