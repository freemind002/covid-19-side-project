package com.example.covid_19.dto;

import java.time.LocalDate;

public class ConfirmedResponseDto {
    private final LocalDate updatedOn;
    private final Long daily; // 假設你在 Model 定義為 Long
    private final Long cumulative; // 假設你在 Model 定義為 Long
    private final Long geographyId;

    // 確保有這個建構子 (對應 Service 裡的參數型態)
    public ConfirmedResponseDto(LocalDate updatedOn, Long daily, Long cumulative, Long geographyId) {
        this.updatedOn = updatedOn;
        this.daily = daily;
        this.cumulative = cumulative;
        this.geographyId = geographyId;
    }

    // Getters
    public LocalDate getUpdatedOn() {
        return updatedOn;
    }

    public Long getDaily() {
        return daily;
    }

    public Long getCumulative() {
        return cumulative;
    }

    public Long getGeographyId() {
        return geographyId;
    }
}