// src/main/java/com/symptocare/backend/dto/TaskResponse.java
package com.symptocare.backend.dto;

import com.symptocare.backend.model.Task;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDate taskDate;
    private LocalTime taskTime;
    private Integer priority;
    private String priorityLabel;
    private String status;

    public static TaskResponse from(Task task) {
        TaskResponse res = new TaskResponse();
        res.setId(task.getId());
        res.setTitle(task.getTitle());
        res.setDescription(task.getDescription());
        res.setTaskDate(task.getTaskDate());
        res.setTaskTime(task.getTaskTime());
        res.setPriority(task.getPriority());
        res.setPriorityLabel(switch (task.getPriority()) {
            case 3 -> "HIGH";
            case 2 -> "MEDIUM";
            default -> "LOW";
        });
        res.setStatus(task.getStatus().name());
        return res;
    }
}