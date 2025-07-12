package com.olive.service;

import com.olive.dto.DailyLogDTO;
import com.olive.dto.TaskTimeSummaryResponse;
import com.olive.dto.TimeLogBreakdownDTO;
import com.olive.dto.TimesheetResponse;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.WorkLog;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.WorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private TeammateRepository teammateRepository;
    @Autowired private TaskRepository taskRepository;

    public TimesheetResponse getTeammateTimesheet(Long teammateId, LocalDate startDate, LocalDate endDate) {
        // Step 1: Fetch the Teammate object first.
        Teammate teammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found with ID: " + teammateId));

        // Step 2: Pass the entire Teammate object to the new repository method.
        List<WorkLog> workLogs = workLogRepository.findByTeammateAndLogDateBetween(teammate, startDate, endDate);

        // Step 3: The rest of the logic remains the same.
        Map<LocalDate, Double> dailyTotals = workLogs.stream()
                .collect(Collectors.groupingBy(
                        WorkLog::getLogDate,
                        Collectors.summingDouble(WorkLog::getHoursSpent)
                ));

        List<DailyLogDTO> dailyLogs = dailyTotals.entrySet().stream()
                .map(entry -> new DailyLogDTO(entry.getKey(), entry.getValue()))
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        double totalHoursForPeriod = dailyLogs.stream()
                .mapToDouble(DailyLogDTO::getTotalHours)
                .sum();

        return new TimesheetResponse(teammateId, teammate.getUser().getFullName(), totalHoursForPeriod, dailyLogs);
    }

    public TaskTimeSummaryResponse getTaskTimeSummary(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        List<WorkLog> workLogs = workLogRepository.findByTaskIdOrderByLogDateDesc(taskId);

        double totalHours = workLogs.stream().mapToDouble(WorkLog::getHoursSpent).sum();

        double devHours = 0;
        double testHours = 0;
        double otherHours = 0;

        for (WorkLog log : workLogs) {
            String group = log.getTeammate().getUser().getRole().getFunctionalGroup();
            if ("DEVELOPER".equals(group) || "DEV_LEAD".equals(group)) {
                devHours += log.getHoursSpent();
            } else if ("TESTER".equals(group) || "TEST_LEAD".equals(group)) {
                testHours += log.getHoursSpent();
            } else {
                otherHours += log.getHoursSpent();
            }
        }

        TimeLogBreakdownDTO breakdown = new TimeLogBreakdownDTO(devHours, testHours, otherHours);
        return new TaskTimeSummaryResponse(taskId, task.getTaskName(), totalHours, breakdown);
    }
}
