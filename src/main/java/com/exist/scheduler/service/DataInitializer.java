package com.exist.scheduler.service;

import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer {

    private final TaskService taskService;

    private final ProjectPlanService projectPlanService;

    public DataInitializer(TaskService taskService, ProjectPlanService projectPlanService) {
        this.taskService = taskService;
        this.projectPlanService = projectPlanService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        ProjectPlan projectPlan1 = new ProjectPlan();

        // Initialize tasks with and without dependencies
        TaskDTO task1 = new TaskDTO(null, "Design", 5, List.of(), projectPlan1); // No dependencies
        TaskDTO task2 = new TaskDTO(null, "Development", 10, List.of(task1), projectPlan1); // Depends on Design
        TaskDTO task3 = new TaskDTO(null, "Testing", 3, List.of(task2), projectPlan1); // Depends on Development
        TaskDTO task4 = new TaskDTO(null, "Review", 4, List.of(), projectPlan1); // No dependencies (can start immediately)

        //To do refactor code for these
//        projectPlan1.setTasks(Arrays.asList(task1, task2, task3, task4));
//        projectPlanService.createProjectPlan(projectPlan1);

        // Save tasks using TaskService
        taskService.saveTasks(Arrays.asList(task1, task2, task3, task4));
    }
}
