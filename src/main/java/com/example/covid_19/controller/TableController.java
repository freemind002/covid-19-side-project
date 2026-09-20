package com.example.covid_19.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dto.TableRecordDto;
import com.example.covid_19.service.ConfirmedService;

@RestController
@RequestMapping("/api/covid/table")
public class TableController {
    private final ConfirmedService confirmedService;

    public TableController(ConfirmedService confirmedService) {
        this.confirmedService = confirmedService;
    }

    // 範例網址：
    // /api/covid/table?startDate=2026-02-01&endDate=2026-03-01&page=0&size=10&sort=updated_on,desc&sort=confirmed_daily,asc
    @GetMapping
    public Page<TableRecordDto> getTableData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> geographyIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) List<String> sort) {

        return confirmedService.getTablePage(startDate, endDate, geographyIds, page, size, sort);
    }
}
