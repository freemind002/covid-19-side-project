package com.example.covid_19.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.covid_19.dao.ConfirmedRepository;
import com.example.covid_19.dto.ConfirmedResponseDto;
import com.example.covid_19.model.Confirmed;

@Service
public class ConfirmedService {

    private final ConfirmedRepository confirmedRepository;

    // 使用建構子注入 (Constructor Injection)
    public ConfirmedService(ConfirmedRepository confirmedRepository) {
        this.confirmedRepository = confirmedRepository;
    }

    public List<ConfirmedResponseDto> getAllConfirmedData() {
        // 相當於 SELECT * FROM confirmed
        List<Confirmed> confirmedList = confirmedRepository.findAll();

        // 將 Model 轉換成 DTO
        return confirmedList.stream()
                .map(c -> new ConfirmedResponseDto(
                        c.getUpdatedOn(),
                        c.getDaily(),
                        c.getCumulative(),
                        c.getGeographyId()))
                .collect(Collectors.toList());
    }
}