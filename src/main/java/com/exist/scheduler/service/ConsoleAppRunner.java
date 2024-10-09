package com.exist.scheduler.service;

import com.exist.scheduler.model.ProjectPlan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ConsoleAppRunner implements CommandLineRunner {

    private final ProjectPlanService projectPlanService;

    public ConsoleAppRunner(ProjectPlanService projectPlanService) {
        this.projectPlanService = projectPlanService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Fetch all project plans
        List<ProjectPlan> projectPlans = projectPlanService.getAllProjectPlans();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        for (ProjectPlan projectPlan : projectPlans) {
            // Output project plan details
            LocalDate[] projectDates = projectPlanService.calculateProjectDates(projectPlan);
            String projectStart = projectDates[0].format(formatter);
            String projectEnd = projectDates[1].format(formatter);

            // Calculate the total duration in days using ChronoUnit.DAYS.between
            long totalDuration = ChronoUnit.DAYS.between(projectDates[0], projectDates[1]);

            System.out.println("Project Plan: " + projectPlan.getName());
            System.out.println("Total Duration: " + totalDuration + " days");
            System.out.println("Project Start: " + projectStart + ", End: " + projectEnd);

            // Output each task in the project plan
            projectPlan.getTasks().forEach(task -> {
                LocalDate[] taskDates = projectPlanService.calculateTaskDates(task);
                String taskStart = taskDates[0].format(formatter);
                String taskEnd = taskDates[1].format(formatter);
                System.out.println("  Task: " + task.getName() + " -> Start: " + taskStart + ", End: " + taskEnd);
            });

            System.out.println(); // New line for better readability
        }
    }
}