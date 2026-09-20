package com.example.covid_19.dto;

import java.time.LocalDate;

public class TableRecordDto {
    private LocalDate updatedOn;
    private Long confirmedDaily;
    private Long confirmedCumulative;
    private Long deathsDaily;
    private Long deathsCumulative;
    private String countryRegion;
    private String provinceState;

    // 💡 1. 務必加上這個無參數建構子（BeanPropertyRowMapper 需要它）
    public TableRecordDto() {
    }

    // 2. 原本的帶參數建構子保留
    public TableRecordDto(LocalDate updatedOn, Long confirmedDaily, Long confirmedCumulative,
            Long deathsDaily, Long deathsCumulative, String countryRegion, String provinceState) {
        this.updatedOn = updatedOn;
        this.confirmedDaily = confirmedDaily;
        this.confirmedCumulative = confirmedCumulative;
        this.deathsDaily = deathsDaily;
        this.deathsCumulative = deathsCumulative;
        this.countryRegion = countryRegion;
        this.provinceState = provinceState;
    }

    public LocalDate getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDate updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getConfirmedDaily() {
        return confirmedDaily;
    }

    public void setConfirmedDaily(Long confirmedDaily) {
        this.confirmedDaily = confirmedDaily;
    }

    public Long getConfirmedCumulative() {
        return confirmedCumulative;
    }

    public void setConfirmedCumulative(Long confirmedCumulative) {
        this.confirmedCumulative = confirmedCumulative;
    }

    public Long getDeathsDaily() {
        return deathsDaily;
    }

    public void setDeathsDaily(Long deathsDaily) {
        this.deathsDaily = deathsDaily;
    }

    public Long getDeathsCumulative() {
        return deathsCumulative;
    }

    public void setDeathsCumulative(Long deathsCumulative) {
        this.deathsCumulative = deathsCumulative;
    }

    public String getCountryRegion() {
        return countryRegion;
    }

    public void setCountryRegion(String countryRegion) {
        this.countryRegion = countryRegion;
    }

    public String getProvinceState() {
        return provinceState;
    }

    public void setProvinceState(String provinceState) {
        this.provinceState = provinceState;
    }
}
