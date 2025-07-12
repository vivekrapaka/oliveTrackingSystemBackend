package com.olive.controller;

import com.olive.dto.TaskTimeSummaryResponse;
import com.olive.dto.TimesheetResponse;
import com.olive.service.PdfGenerationService;
import com.olive.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportingController {

    @Autowired private ReportingService reportingService;
    @Autowired private PdfGenerationService pdfGenerationService;

    @GetMapping("/timesheet")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER', 'TEST_MANAGER', 'DEV_LEAD', 'TEST_LEAD')")
    public ResponseEntity<TimesheetResponse> getTeammateTimesheet(
            @RequestParam Long teammateId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportingService.getTeammateTimesheet(teammateId, startDate, endDate));
    }

    @GetMapping("/task-summary/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER', 'TEST_MANAGER', 'DEV_LEAD', 'TEST_LEAD')")
    public ResponseEntity<TaskTimeSummaryResponse> getTaskTimeSummary(@PathVariable Long taskId) {
        return ResponseEntity.ok(reportingService.getTaskTimeSummary(taskId));
    }

    @GetMapping(value = "/timesheet/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER', 'TEST_MANAGER', 'DEV_LEAD', 'TEST_LEAD')")
    public ResponseEntity<InputStreamResource> downloadTimesheetPdf(
            @RequestParam Long teammateId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TimesheetResponse timesheet = reportingService.getTeammateTimesheet(teammateId, startDate, endDate);
        ByteArrayInputStream bis = pdfGenerationService.generateTimesheetPdf(timesheet);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=timesheet.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    @GetMapping(value = "/task-summary/{taskId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'DEV_MANAGER', 'TEST_MANAGER', 'DEV_LEAD', 'TEST_LEAD')")
    public ResponseEntity<InputStreamResource> downloadTaskSummaryPdf(@PathVariable Long taskId) {
        TaskTimeSummaryResponse summary = reportingService.getTaskTimeSummary(taskId);
        ByteArrayInputStream bis = pdfGenerationService.generateTaskSummaryPdf(summary);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=task_summary.pdf");
        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }
}
