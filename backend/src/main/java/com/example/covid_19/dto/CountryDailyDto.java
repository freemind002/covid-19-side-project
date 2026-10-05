package com.example.covid_19.dto;

import java.time.LocalDate;

public class CountryDailyDto {
    private Long geographicsId; // 對應 geographics 表的 id
    private String countryRegion;
    private String provinceState;
    private LocalDate date;
    private Long confirmed;
    private Long deaths;

    // Getters and Setters

    public Long getGeographicsId() {
        return geographicsId;
    }

    public void setGeographicsId(Long geographicsId) {
        this.geographicsId = geographicsId;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Long confirmed) {
        this.confirmed = confirmed;
    }

    public Long getDeaths() {
        return deaths;
    }

    public void setDeaths(Long deaths) {
        this.deaths = deaths;
    }
}