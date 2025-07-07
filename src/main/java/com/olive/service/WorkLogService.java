package com.olive.service;

import com.olive.dto.WorkLogRequest;
import com.olive.dto.WorkLogResponse;
import com.olive.model.Task;
import com.olive.model.Teammate;
import com.olive.model.WorkLog;
import com.olive.repository.TaskRepository;
import com.olive.repository.TeammateRepository;
import com.olive.repository.WorkLogRepository;
import com.olive.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WorkLogService {

    @Autowired private WorkLogRepository workLogRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private TeammateRepository teammateRepository;

    public WorkLogResponse logWork(Long taskId, WorkLogRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Teammate teammate = teammateRepository.findById(userDetails.getId()) // Assuming user ID and teammate ID are linked this way
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teammate not found"));

        WorkLog workLog = new WorkLog();
        workLog.setTask(task);
        workLog.setTeammate(teammate);
        workLog.setHoursSpent(request.getHoursSpent());
        workLog.setLogDate(request.getLogDate());
        workLog.setDescription(request.getDescription());

        WorkLog savedLog = workLogRepository.save(workLog);
        return convertToDto(savedLog);
    }

    public List<WorkLogResponse> getWorkLogsForTask(Long taskId) {
        return workLogRepository.findByTaskIdOrderByLogDateDesc(taskId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private WorkLogResponse convertToDto(WorkLog workLog) {
        return new WorkLogResponse(
                workLog.getId(),
                workLog.getHoursSpent(),
                workLog.getLogDate(),
                workLog.getDescription(),
                workLog.getTeammate().getTeammateId(),
                workLog.getTeammate().getName()
        );
    }
}
