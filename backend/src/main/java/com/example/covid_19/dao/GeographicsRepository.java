package com.example.covid_19.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.covid_19.model.Geographics;

@Repository
public interface GeographicsRepository extends JpaRepository<Geographics, Long> {
    // 💡 透過 Spring Data JPA 的命名規則，自動依 countryRegion 與 provinceState 進行升冪排序
    List<Geographics> findAllByOrderByCountryRegionAscProvinceStateAsc();
}