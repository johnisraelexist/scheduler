package com.exist.scheduler.service;

import com.exist.scheduler.dto.TaskDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private final TaskService taskService;

    public DataInitializer(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostConstruct
    public void init() {
        // Initialize tasks with dependencies as DTOs
        TaskDTO task1 = new TaskDTO(null, "Design", 5, List.of());
        TaskDTO task2 = new TaskDTO(null, "Development", 10, List.of(task1));
        TaskDTO task3 = new TaskDTO(null, "Testing", 3, List.of(task2));

        // Save tasks using TaskService
        taskService.saveTasks(Arrays.asList(task1, task2, task3));
    }
}
