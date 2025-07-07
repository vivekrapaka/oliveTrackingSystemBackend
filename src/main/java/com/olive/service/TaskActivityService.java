package com.olive.service;

import com.olive.dto.TaskActivityResponse;
import com.olive.model.Task;
import com.olive.model.TaskActivity;
import com.olive.model.User;
import com.olive.repository.TaskActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskActivityService {

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    public void logChange(Task task, User user, String fieldName, String oldValue, String newValue) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setUser(user);
        activity.setEventType("FIELD_UPDATE");
        activity.setFieldName(fieldName);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        taskActivityRepository.save(activity);
    }

    public void addComment(Task task, User user, String content) {
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setUser(user);
        activity.setEventType("COMMENT");
        activity.setContent(content);
        taskActivityRepository.save(activity);
    }

    public List<TaskActivityResponse> getTaskHistory(Long taskId) {
        return taskActivityRepository.findByTaskIdOrderByTimestampDesc(taskId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TaskActivityResponse convertToDto(TaskActivity activity) {
        return new TaskActivityResponse(
                activity.getId(),
                activity.getEventType(),
                activity.getContent(),
                activity.getFieldName(),
                activity.getOldValue(),
                activity.getNewValue(),
                activity.getUser() != null ? activity.getUser().getFullName() : "System",
                activity.getTimestamp()
        );
    }
}