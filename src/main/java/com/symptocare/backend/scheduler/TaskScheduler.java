package com.symptocare.backend.scheduler;

import com.symptocare.backend.model.Notification.NotificationType;
import com.symptocare.backend.model.Task;
import com.symptocare.backend.model.Task.TaskStatus;
import com.symptocare.backend.model.User;
import com.symptocare.backend.repository.TaskRepository;
import com.symptocare.backend.repository.UserRepository;
import com.symptocare.backend.service.NotificationService;
import com.symptocare.backend.service.OpenAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskScheduler {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OpenAIService openAIService;

    // ─── Task Reminder ────────────────────────────────────────────────────────
    // Runs every minute — checks tasks due in next 1 minute
    @Scheduled(fixedRate = 60000)
    public void sendTaskReminders() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalTime oneMinuteLater = now.plusMinutes(1);

        log.debug("Checking due tasks between {} and {}", now, oneMinuteLater);

        List<Task> dueTasks = taskRepository.findDueTasks(today, now, oneMinuteLater);

        for (Task task : dueTasks) {
            try {
                User user = task.getUser();

                // Skip if already sent for this task
                boolean alreadySent = notificationService
                        .hasNotificationBeenSent(user, task.getId(), NotificationType.TASK_REMINDER);

                if (alreadySent) continue;

                // Get motivational message from Groq AI
                String motivationalMessage = openAIService.getMotivationalQuote(task.getTitle());

                // Create notification and push via WebSocket
                notificationService.createAndPush(
                        user,
                        "⏰ Time for: " + task.getTitle(),
                        motivationalMessage,
                        NotificationType.TASK_REMINDER,
                        task.getId()
                );

                // Mark notification as sent on task
                task.setNotificationSent(true);
                taskRepository.save(task);

                log.info("Task reminder sent → task: {} | user: {}", task.getTitle(), user.getEmail());

            } catch (Exception e) {
                log.error("Failed to send reminder for task {}: {}", task.getId(), e.getMessage());
            }
        }
    }

    // ─── Overdue Task Check ───────────────────────────────────────────────────
    // Runs every hour — checks tasks that passed their time and are still PENDING
    @Scheduled(fixedRate = 3600000)
    public void checkOverdueTasks() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<Task> overdueTasks = taskRepository.findOverdueTasks(today, now);

        for (Task task : overdueTasks) {
            try {
                User user = task.getUser();

                // Skip if overdue notification already sent
                boolean alreadySent = notificationService
                        .hasNotificationBeenSent(user, task.getId(), NotificationType.TASK_OVERDUE);

                if (alreadySent) continue;

                notificationService.createAndPush(
                        user,
                        "❗ Overdue: " + task.getTitle(),
                        "Yeh task abhi bhi pending hai! \"" + task.getTitle() +
                                "\" complete karo — kal ke liye mat chodo! 🔥",
                        NotificationType.TASK_OVERDUE,
                        task.getId()
                );

                log.info("Overdue notification sent → task: {} | user: {}", task.getTitle(), user.getEmail());

            } catch (Exception e) {
                log.error("Failed to send overdue notification for task {}: {}", task.getId(), e.getMessage());
            }
        }
    }

    // ─── Morning Motivational Push ────────────────────────────────────────────
    // Runs every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendMorningMotivation() {
        log.info("Sending morning motivational notifications to all users");

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            try {
                // Count today's pending tasks for this user
                long pendingCount = taskRepository
                        .countByUserAndTaskDateAndStatus(user, LocalDate.now(), TaskStatus.PENDING);

                String message;
                if (pendingCount > 0) {
                    message = openAIService.getMotivationalQuote(
                            "starting my day with " + pendingCount + " tasks to complete"
                    );
                } else {
                    message = "Good morning! 🌅 Aaj ka din ekdum fresh start hai. " +
                            "Apne goals set karo aur din ko productive banao! 💪";
                }

                notificationService.createAndPush(
                        user,
                        "🌅 Good Morning! Aaj ka plan ready hai?",
                        message,
                        NotificationType.MOTIVATIONAL,
                        null
                );

            } catch (Exception e) {
                log.error("Failed morning motivation for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    // ─── Night Summary ────────────────────────────────────────────────────────
    // Runs every day at 9:00 PM
    @Scheduled(cron = "0 0 21 * * *")
    public void sendNightSummary() {
        log.info("Sending night summary notifications");

        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            try {
                long completedCount = taskRepository
                        .countByUserAndTaskDateAndStatus(user, LocalDate.now(), TaskStatus.COMPLETED);

                long pendingCount = taskRepository
                        .countByUserAndTaskDateAndStatus(user, LocalDate.now(), TaskStatus.PENDING);

                String message;

                if (completedCount == 0 && pendingCount == 0) {
                    // No tasks today
                    message = "Aaj koi task nahi tha. Kal ke liye apna planner ready karo! 📋";
                } else if (pendingCount == 0) {
                    // All done!
                    message = "Waah! 🎉 Aaj ke saare " + completedCount +
                            " tasks complete kar diye! Tum ekdum champion ho! 🏆";
                } else {
                    // Some pending
                    message = completedCount + " tasks complete kiye, lekin " + pendingCount +
                            " abhi bhi pending hain. Kal inhe zaroor complete karo! 💪";
                }

                notificationService.createAndPush(
                        user,
                        "🌙 Aaj ka Summary",
                        message,
                        NotificationType.MOTIVATIONAL,
                        null
                );

            } catch (Exception e) {
                log.error("Failed night summary for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    // ─── Weekly Cleanup ───────────────────────────────────────────────────────
    // Runs every Sunday at midnight — clean old read notifications
    @Scheduled(cron = "0 0 0 * * SUN")
    public void cleanOldNotifications() {
        log.info("Running weekly notification cleanup");
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            try {
                notificationService.cleanOldNotifications(user);
            } catch (Exception e) {
                log.error("Cleanup failed for user {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }
}