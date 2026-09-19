package com.example.covid_19.dto;

import java.time.LocalDate;

public class ConfirmedSummaryDto {
    private final Long totalDailySum; // 指定區間與地區內的 daily 總和
    private final LocalDate startDate; // 開始日期
    private final LocalDate endDate; // 結束日期

    public ConfirmedSummaryDto(Long totalDailySum, LocalDate startDate, LocalDate endDate) {
        this.totalDailySum = totalDailySum;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public Long getTotalDailySum() {
        return totalDailySum;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}