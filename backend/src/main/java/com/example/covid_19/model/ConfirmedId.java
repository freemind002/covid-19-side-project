package com.example.covid_19.model;

import java.io.Serializable;
import java.util.Objects;

public class ConfirmedId implements Serializable {
    private Long geographyId;
    private java.time.LocalDate updatedOn;

    // 1. 預設建構子
    public ConfirmedId() {
    }

    public ConfirmedId(Long geographyId, java.time.LocalDate updatedOn) {
        this.geographyId = geographyId;
        this.updatedOn = updatedOn;
    }

    // 2. 必須實作 equals (記得要有 return)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfirmedId that = (ConfirmedId) o;
        return Objects.equals(geographyId, that.geographyId) &&
                Objects.equals(updatedOn, that.updatedOn);
    }

    // 3. 必須實作 hashCode (記得要有 return)
    @Override
    public int hashCode() {
        return Objects.hash(geographyId, updatedOn);
    }
}