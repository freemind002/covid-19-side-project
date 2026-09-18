package com.example.covid_19.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dto.ConfirmedResponseDto;
import com.example.covid_19.dto.ConfirmedSingleDayDto;
import com.example.covid_19.dto.ConfirmedSummaryDto;
import com.example.covid_19.service.ConfirmedService;

@RestController
@RequestMapping("/api/confirmed")
public class ConfirmedController {

    private final ConfirmedService confirmedService;

    public ConfirmedController(ConfirmedService confirmedService) {
        this.confirmedService = confirmedService;
    }

    @GetMapping
    public ResponseEntity<List<ConfirmedResponseDto>> getAllConfirmed() {
        List<ConfirmedResponseDto> data = confirmedService.getAllConfirmedData();
        return ResponseEntity.ok(data);
    }

    // 範例網址 1 (無地區篩選):
    // /api/confirmed/summary?startDate=2026-01-01&endDate=2026-01-31
    // 範例網址 2 (有地區篩選):
    // /api/confirmed/summary?startDate=2026-01-01&endDate=2026-01-31&geographyIds=1,2,3
    @GetMapping("/summary")
    public ConfirmedSummaryDto getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> geographyIds) {

        return confirmedService.getSummary(startDate, endDate, geographyIds);
    }

    // 範例網址 1 (全區單日): /api/confirmed/daily?date=2026-09-17
    // 範例網址 2 (指定地區單日): /api/confirmed/daily?date=2026-09-17&geographyIds=1,2
    @GetMapping("/daily")
    public ConfirmedSingleDayDto getDailyByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<Long> geographyIds) {

        return confirmedService.getDailySummaryByDate(date, geographyIds);
    }
}
