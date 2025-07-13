package com.olive.service;

import com.olive.dto.*;
import com.olive.model.Project;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.WorkLog;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.WorkLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private TeammateRepository teammateRepository;
    @Autowired private TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public TimesheetResponse getTeammateTimesheet(Long teammateId, LocalDate startDate, LocalDate endDate) {
        Teammate teammate = teammateRepository.findById(teammateId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found"));

        List<WorkLog> workLogs = workLogRepository.findByTeammateAndLogDateBetween(teammate, startDate, endDate);

        Map<LocalDate, List<WorkLog>> groupedByDate = workLogs.stream()
                .collect(Collectors.groupingBy(WorkLog::getLogDate));

        List<DailyLogDTO> dailyLogs = groupedByDate.entrySet().stream()
                .map(entry -> {
                    double dailyTotal = entry.getValue().stream().mapToDouble(WorkLog::getHoursSpent).sum();
                    List<TaskLogDetailDTO> taskDetails = entry.getValue().stream()
                            .collect(Collectors.groupingBy(log -> log.getTask().getTaskName(), Collectors.summingDouble(WorkLog::getHoursSpent)))
                            .entrySet().stream()
                            .map(taskEntry -> new TaskLogDetailDTO(taskEntry.getKey(), taskEntry.getValue()))
                            .collect(Collectors.toList());
                    return new DailyLogDTO(entry.getKey(), dailyTotal, taskDetails);
                })
                .sorted((d1, d2) -> d1.getDate().compareTo(d2.getDate()))
                .collect(Collectors.toList());

        double totalHoursForPeriod = dailyLogs.stream().mapToDouble(DailyLogDTO::getTotalHours).sum();
        return new TimesheetResponse(teammateId, teammate.getUser().getFullName(), totalHoursForPeriod, dailyLogs);
    }

    @Transactional(readOnly = true)
    public TaskTimeSummaryResponse getTaskTimeSummary(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        List<WorkLog> workLogs = workLogRepository.findByTaskIdOrderByLogDateDesc(taskId);

        String devManager = findManagerForProject(task.getProject(), "DEV_MANAGER");
        String testManager = findManagerForProject(task.getProject(), "TEST_MANAGER");

        double totalHours = 0;
        double devHours = 0;
        double testHours = 0;
        double otherHours = 0;

        Set<String> developerNames = new HashSet<>();
        Set<String> testerNames = new HashSet<>();

        for (WorkLog log : workLogs) {
            double hours = log.getHoursSpent();
            totalHours += hours;
            String group = log.getTeammate().getUser().getRole().getFunctionalGroup();
            String name = log.getTeammate().getUser().getFullName();

            if ("DEVELOPER".equals(group) || "DEV_LEAD".equals(group)) {
                devHours += hours;
                developerNames.add(name);
            } else if ("TESTER".equals(group) || "TEST_LEAD".equals(group)) {
                testHours += hours;
                testerNames.add(name);
            } else {
                otherHours += hours;
            }
        }

        TimeLogBreakdownDTO breakdown = new TimeLogBreakdownDTO(
                devHours,
                task.getDevelopmentDueHours(),
                testHours,
                task.getTestingDueHours()
        );

        return new TaskTimeSummaryResponse(taskId, task.getTaskName(), totalHours, breakdown, devManager, testManager, new ArrayList<>(developerNames), new ArrayList<>(testerNames), task.getDevelopmentDueHours(), task.getTestingDueHours());
    }

    private String findManagerForProject(Project project, String functionalGroup) {
        return project.getTeammates().stream()
                .filter(t -> functionalGroup.equals(t.getUser().getRole().getFunctionalGroup()))
                .map(t -> t.getUser().getFullName())
                .findFirst()
                .orElse("N/A");
    }
}
