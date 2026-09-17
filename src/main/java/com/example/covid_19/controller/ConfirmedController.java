package com.example.covid_19.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dto.ConfirmedResponseDto;
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
}
