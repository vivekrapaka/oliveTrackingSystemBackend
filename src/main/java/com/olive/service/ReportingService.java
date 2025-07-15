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
import java.util.List;
import java.util.Map;
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

        double totalHours = workLogs.stream().mapToDouble(WorkLog::getHoursSpent).sum();

        // Group logs by teammate to sum up individual efforts
        Map<Teammate, Double> effortByTeammate = workLogs.stream()
                .collect(Collectors.groupingBy(WorkLog::getTeammate, Collectors.summingDouble(WorkLog::getHoursSpent)));

        // Classify each teammate's effort into developer or tester lists
        List<TeammateEffortDTO> developerEffort = new ArrayList<>();
        List<TeammateEffortDTO> testerEffort = new ArrayList<>();

        effortByTeammate.forEach((teammate, hours) -> {
            String group = teammate.getUser().getRole().getFunctionalGroup();
            String name = teammate.getUser().getFullName();
            if ("DEVELOPER".equals(group) || "DEV_LEAD".equals(group)) {
                developerEffort.add(new TeammateEffortDTO(name, hours));
            } else if ("TESTER".equals(group) || "TEST_LEAD".equals(group)) {
                testerEffort.add(new TeammateEffortDTO(name, hours));
            }
        });

        return new TaskTimeSummaryResponse(taskId, task.getTaskName(), totalHours, devManager, testManager, task.getDevelopmentDueHours(), task.getTestingDueHours(), developerEffort, testerEffort);
    }

    private String findManagerForProject(Project project, String functionalGroup) {
        return project.getTeammates().stream()
                .filter(t -> functionalGroup.equals(t.getUser().getRole().getFunctionalGroup()))
                .map(t -> t.getUser().getFullName())
                .findFirst()
                .orElse("N/A");
    }
}
