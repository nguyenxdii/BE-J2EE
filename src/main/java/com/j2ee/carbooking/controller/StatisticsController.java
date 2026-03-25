package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.service.ExcelExportService;
import com.j2ee.carbooking.service.StatisticsService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ExcelExportService excelExportService;

    public StatisticsController(StatisticsService statisticsService, ExcelExportService excelExportService) {
        this.statisticsService = statisticsService;
        this.excelExportService = excelExportService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AppApiResponse<Map<String, Object>>> getDashboard() {
        return ResponseEntity.ok(AppApiResponse.success("Lấy số liệu dashboard thành công",
            statisticsService.getDashboardOverview()));
    }

    @GetMapping("/revenue")
    public ResponseEntity<AppApiResponse<Map<String, Object>>> getRevenue(
            @RequestParam(defaultValue = "3") int month,
            @RequestParam(defaultValue = "2026") int year) {
        
        return ResponseEntity.ok(AppApiResponse.success("Lấy báo cáo doanh thu thành công",
            statisticsService.getRevenueStats(month, year)));
    }

    @GetMapping("/orders")
    public ResponseEntity<AppApiResponse<Map<String, Object>>> getOrderStats() {
        return ResponseEntity.ok(AppApiResponse.success("Lấy báo cáo đơn hàng & xe thành công",
            statisticsService.getOrderVehicleStats()));
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        ByteArrayInputStream in = excelExportService.exportOrders();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=orders_report.xlsx");

        return ResponseEntity
            .ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(in));
    }
}
