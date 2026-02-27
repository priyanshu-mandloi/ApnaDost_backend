// src/main/java/com/symptocare/backend/controller/TaskController.java
package com.symptocare.backend.controller;

import com.symptocare.backend.dto.TaskRequest;
import com.symptocare.backend.dto.TaskResponse;
import com.symptocare.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    // POST /api/tasks → create task
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            Authentication auth,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(auth.getName(), request));
    }

    // GET /api/tasks → all tasks sorted by priority
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(Authentication auth) {
        return ResponseEntity.ok(taskService.getAllTasks(auth.getName()));
    }

    // GET /api/tasks/today → today's tasks
    @GetMapping("/today")
    public ResponseEntity<List<TaskResponse>> getTodayTasks(Authentication auth) {
        return ResponseEntity.ok(taskService.getTodayTasks(auth.getName()));
    }

    // GET /api/tasks/date?date=2025-06-01 → tasks for a specific date
    @GetMapping("/date")
    public ResponseEntity<List<TaskResponse>> getTasksByDate(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(taskService.getTasksByDate(auth.getName(), date));
    }

    // PUT /api/tasks/{id} → update task
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(auth.getName(), id, request));
    }

    // PATCH /api/tasks/{id}/complete → mark as done
    @PatchMapping("/{id}/complete")
    public ResponseEntity<TaskResponse> markComplete(
            Authentication auth,
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.markComplete(auth.getName(), id));
    }

    // DELETE /api/tasks/{id} → delete task
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(
            Authentication auth,
            @PathVariable Long id) {
        taskService.deleteTask(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}