package com.exist.scheduler.service;

import com.exist.scheduler.dto.TaskDTO;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class ConsoleAppRunner implements CommandLineRunner {

    private final TaskService taskService;
    private final TaskScheduler taskScheduler;

    public ConsoleAppRunner(TaskService taskService, TaskScheduler taskScheduler) {
        this.taskService = taskService;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void run(String... args) throws Exception {
        // Fetch tasks from the service
        List<TaskDTO> tasks = taskService.getAllTasks();

        // Calculate the schedule
        Map<TaskDTO, LocalDate[]> schedule = taskScheduler.calculateSchedule(tasks);

        // Output the schedule
        System.out.println("Project Task Schedule:");
        schedule.forEach((task, dates) -> {
            System.out.println(task.getName() + " -> Start: " + dates[0] + ", End: " + dates[1]);
        });
    }
}
