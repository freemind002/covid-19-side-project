package com.example.covid_19.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // 💡 引入 Spring Page
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.covid_19.dao.GeographicsRepository;
import com.example.covid_19.dto.CountryDailyDto;
import com.example.covid_19.dto.TableRecordDto; // 💡 引入分頁表格的 DTO
import com.example.covid_19.model.Geographics;
import com.example.covid_19.service.CovidOperatorService;

@RestController
@RequestMapping("/api/covid/operator")
public class OperatorCovidController {

    @Autowired
    private CovidOperatorService covidOperatorService;

    @Autowired
    private GeographicsRepository geographicsRepository;

    // 1. 讀取某個國家/省份的每日疫情數據列表 (Read - 舊有的維持不變)
    @GetMapping("/daily-records")
    @PreAuthorize("hasAuthority('covid.update')") // 確保只有 operator/admin 能操作
    public ResponseEntity<?> getDailyRecords(
            @RequestParam Long geographicsId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        List<CountryDailyDto> records = covidOperatorService.getRecords(geographicsId, startDate, endDate);
        return ResponseEntity.ok(records);
    }

    // 💡 4. 新增：取得分頁、排序與動態累計的疫情數據表格 (對應前端的表格分頁查詢)
    @GetMapping("/table-page")
    @PreAuthorize("hasAuthority('covid.update')")
    public ResponseEntity<Page<TableRecordDto>> getTablePage(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) List<Long> geographyIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) List<String> sort) {

        Page<TableRecordDto> pageResult = covidOperatorService.getOperatorTablePage(
                startDate, endDate, geographyIds, page, size, sort);
        return ResponseEntity.ok(pageResult);
    }

    // 2. 新增或修改某日的綜合疫情數據 (Create / Update / Upsert)
    @PutMapping("/daily-records")
    @PreAuthorize("hasAuthority('covid.update')")
    public ResponseEntity<?> upsertDailyRecord(@RequestBody CountryDailyDto dto) {
        covidOperatorService.saveOrUpdateRecord(dto);
        return ResponseEntity.ok("疫情數據儲存成功");
    }

    // 3. 刪除某日的疫情數據 (Delete)
    @DeleteMapping("/daily-records")
    @PreAuthorize("hasAuthority('covid.delete')")
    public ResponseEntity<?> deleteDailyRecord(
            @RequestParam Long geographicsId,
            @RequestParam LocalDate date) {

        covidOperatorService.deleteRecord(geographicsId, date);
        return ResponseEntity.ok("疫情數據刪除成功");
    }

    // 取得所有地區清單供前端下拉選單使用
    @GetMapping("/geographies")
    public List<Geographics> getAllGeographies() {
        // 呼叫排序後的查詢方法
        return geographicsRepository.findAllByOrderByCountryRegionAscProvinceStateAsc();
    }
}