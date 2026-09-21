package com.example.covid_19.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "deaths")
@IdClass(DeathsId.class)
public class Deaths {

    @Id
    @Column(name = "geography_id")
    private Long geographyId;

    @Id
    @Column(name = "updated_on")
    private LocalDate updatedOn;

    @Column(name = "daily")
    private Long daily;
    @Column(name = "cumulative")
    private Long cumulative;

    public Long getGeographyId() {
        return geographyId;
    }

    public void setGeographyId(Long geographyId) {
        this.geographyId = geographyId;
    }

    public LocalDate getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDate updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Long getDaily() {
        return daily;
    }

    public void setDaily(Long daily) {
        this.daily = daily;
    }

    public Long getCumulative() {
        return cumulative;
    }

    public void setCumulative(Long cumulative) {
        this.cumulative = cumulative;
    }

}
