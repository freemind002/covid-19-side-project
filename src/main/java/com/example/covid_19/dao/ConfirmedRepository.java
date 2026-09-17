package com.example.covid_19.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.covid_19.model.Confirmed;
import com.example.covid_19.model.ConfirmedId;

public interface ConfirmedRepository extends JpaRepository<Confirmed, ConfirmedId> {
    // 這裡的主鍵型態從 Long 改成了 ConfirmedId
}