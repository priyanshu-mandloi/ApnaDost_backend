package com.symptocare.backend.repository;

import com.symptocare.backend.model.Task;
import com.symptocare.backend.model.Task.TaskStatus;
import com.symptocare.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // All tasks for user sorted by priority DESC then time ASC
    List<Task> findByUserOrderByPriorityDescTaskTimeAsc(User user);

    // Tasks for a specific date sorted by priority then time
    List<Task> findByUserAndTaskDateOrderByPriorityDescTaskTimeAsc(User user, LocalDate date);

    // ─── Used by TaskScheduler - sendTaskReminders() ──────────────────────────
    // Find tasks due in the next minute that haven't sent notification yet
    @Query("SELECT t FROM Task t WHERE t.taskDate = :date " +
           "AND t.taskTime BETWEEN :from AND :to " +
           "AND t.notificationSent = false " +
           "AND t.status != 'COMPLETED'")
    List<Task> findDueTasks(
            @Param("date") LocalDate date,
            @Param("from") LocalTime from,
            @Param("to") LocalTime to
    );

    // ─── Used by TaskScheduler - checkOverdueTasks() ──────────────────────────
    // Tasks that are past their time, still PENDING, notification not sent yet
    @Query("SELECT t FROM Task t WHERE t.taskDate = :date " +
           "AND t.taskTime < :time " +
           "AND t.status = 'PENDING' " +
           "AND t.notificationSent = false")
    List<Task> findOverdueTasks(
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );

    // ─── Used by TaskScheduler - sendMorningMotivation() & sendNightSummary() ─
    // Count tasks by user, date and status
    long countByUserAndTaskDateAndStatus(User user, LocalDate taskDate, TaskStatus status);
}