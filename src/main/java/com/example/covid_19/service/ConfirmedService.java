package com.example.covid_19.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.covid_19.dao.ConfirmedRepository;
import com.example.covid_19.dto.ConfirmedResponseDto;
import com.example.covid_19.dto.ConfirmedSummaryDto;
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

    public ConfirmedSummaryDto getSummary(LocalDate startDate, LocalDate endDate, List<Long> geographyIds) {
        // 1. 防呆驗證：如果日期有填，且開始日期大於結束日期，直接擋下！
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "開始日期不能大於結束日期！");
        }
        // 如果前端沒有傳入 geographyIds 或者是空的，傳 null 讓 JPQL 的 :geographyIds IS NULL 生效
        List<Long> targetIds = (geographyIds == null || geographyIds.isEmpty()) ? null : geographyIds;

        return confirmedRepository.getConfirmedSummary(startDate, endDate, targetIds);
    }
}