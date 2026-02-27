package com.symptocare.backend.service;

import com.symptocare.backend.dto.TaskRequest;
import com.symptocare.backend.dto.TaskResponse;
import com.symptocare.backend.model.Task;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.TaskRepository;
import com.symptocare.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create a new task
    public TaskResponse createTask(String email, TaskRequest request) {
        User user = getUser(email);
        Task task = Task.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .taskDate(request.getTaskDate())
                .taskTime(request.getTaskTime())
                .priority(request.getPriority() != null ? request.getPriority() : 1)
                .status(Task.TaskStatus.PENDING)
                .notificationSent(false)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    // Get all tasks (sorted by priority HIGH > MEDIUM > LOW)
    public List<TaskResponse> getAllTasks(String email) {
        User user = getUser(email);
        return taskRepository.findByUserOrderByPriorityDescTaskTimeAsc(user)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    // Get tasks for today specifically
    public List<TaskResponse> getTodayTasks(String email) {
        User user = getUser(email);
        return taskRepository.findByUserAndTaskDateOrderByPriorityDescTaskTimeAsc(user, LocalDate.now())
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    // Get tasks for a specific date
    public List<TaskResponse> getTasksByDate(String email, LocalDate date) {
        User user = getUser(email);
        return taskRepository.findByUserAndTaskDateOrderByPriorityDescTaskTimeAsc(user, date)
                .stream()
                .map(TaskResponse::from)
                .collect(Collectors.toList());
    }

    // Update task
    public TaskResponse updateTask(String email, Long taskId, TaskRequest request) {
        User user = getUser(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Security: ensure task belongs to this user
        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTaskDate(request.getTaskDate());
        task.setTaskTime(request.getTaskTime());
        if (request.getPriority() != null) task.setPriority(request.getPriority());
        task.setNotificationSent(false); // reset so it notifies again if time changed

        return TaskResponse.from(taskRepository.save(task));
    }

    // Mark task as complete
    public TaskResponse markComplete(String email, Long taskId) {
        User user = getUser(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        task.setStatus(Task.TaskStatus.COMPLETED);
        return TaskResponse.from(taskRepository.save(task));
    }

    // Delete task
    public void deleteTask(String email, Long taskId) {
        User user = getUser(email);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!task.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        taskRepository.delete(task);
    }
}