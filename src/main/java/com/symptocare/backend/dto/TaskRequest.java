// src/main/java/com/symptocare/backend/dto/TaskRequest.java
package com.symptocare.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Task date is required")
    private LocalDate taskDate;

    @NotNull(message = "Task time is required")
    private LocalTime taskTime;

    // 1=LOW, 2=MEDIUM, 3=HIGH
    @Min(1) @Max(3)
    private Integer priority;
}