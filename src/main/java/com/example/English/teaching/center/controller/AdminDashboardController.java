package com.example.English.teaching.center.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.English.teaching.center.model.dto.MonthlyRevenueDTO;
import com.example.English.teaching.center.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {
    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }   

    @GetMapping("/revenue-chart")
    public ResponseEntity<List<MonthlyRevenueDTO>> getRevenueChartData(
            @RequestParam(required = false) Integer year) { 

        List<MonthlyRevenueDTO> data = adminService.getChartData(year);
        return ResponseEntity.ok(data);
    }
}
