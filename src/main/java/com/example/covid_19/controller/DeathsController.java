package com.example.covid_19.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dto.DeathsSingleDayDto;
import com.example.covid_19.dto.DeathsSummaryDto;
import com.example.covid_19.service.DeathsService;

@RestController
@RequestMapping("/api/deaths")
public class DeathsController {
    private final DeathsService deathsService;

    public DeathsController(DeathsService deathsService) {
        this.deathsService = deathsService;
    }

    // 範例網址 1 (無地區篩選):
    // /api/deaths/summary?startDate=2026-01-01&endDate=2026-01-31
    // 範例網址 2 (有地區篩選):
    // /api/deaths/summary?startDate=2026-01-01&endDate=2026-01-31&geographyIds=1,2,3
    @GetMapping("/summary")
    public DeathsSummaryDto getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> geographyIds) {

        return deathsService.getSummary(startDate, endDate, geographyIds);
    }

    // 範例網址 1 (全區單日): /api/deaths/daily?date=2026-09-17
    // 範例網址 2 (指定地區單日): /api/deaths/daily?date=2026-09-17&geographyIds=1,2
    @GetMapping("/daily")
    public DeathsSingleDayDto getDailyByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> geographyIds) {
        return deathsService.getDailySummaryByDate(date, geographyIds);
    }
}
