package com.olive.controller;

import com.olive.dto.DashboardSummaryResponse;
import com.olive.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/api/dashboard")
 //   @CrossOrigin(origins = {"http://localhost:8080", "http://localhost:8085"})
    public class DashboardController {


    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final DashboardService dashboardService;

    @Autowired
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        logger.info("Received request for dashboard summary.");
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        logger.info("Returning dashboard summary.");
        return ResponseEntity.ok(response);
    }
}
