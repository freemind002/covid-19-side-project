package com.example.covid_19.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.covid_19.dao.DeathsRepository;
import com.example.covid_19.dto.DeathsSingleDayDto;
import com.example.covid_19.dto.DeathsSummaryDto;

@Service
public class DeathsService {
    private final DeathsRepository deathsRepository;

    // 使用建構子注入 (Constructor Injection)
    public DeathsService(DeathsRepository deathsRepository) {
        this.deathsRepository = deathsRepository;
    }

    public DeathsSummaryDto getSummary(LocalDate startDate, LocalDate endDate, List<Long> geographyIds) {
        // 1. 防呆驗證：如果日期有填，且開始日期大於結束日期，直接擋下！
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "開始日期不能大於結束日期！");
        }
        // 如果前端沒有傳入 geographyIds 或者是空的，傳 null 讓 JPQL 的 :geographyIds IS NULL 生效
        List<Long> targetIds = (geographyIds == null || geographyIds.isEmpty()) ? null : geographyIds;

        return deathsRepository.getDeathsSummary(startDate, endDate, targetIds);

    }

    public DeathsSingleDayDto getDailySummaryByDate(LocalDate targetDate, List<Long> geographyIds) {
        if (targetDate == null) {
            throw new IllegalArgumentException("必需指定查詢日期！");
        }

        List<Long> targetIds = (geographyIds == null || geographyIds.isEmpty()) ? null : geographyIds;
        
        return deathsRepository.getDeathsByDate(targetDate, targetIds); 
    }
}
