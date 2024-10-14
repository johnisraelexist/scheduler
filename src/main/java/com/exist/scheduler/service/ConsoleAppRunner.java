package com.exist.scheduler.service;

import com.exist.scheduler.model.ProjectPlan;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ConsoleAppRunner implements CommandLineRunner {

    private final ProjectPlanService projectPlanService;

    Logger logger = LogManager.getLogger(ConsoleAppRunner.class);

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

                logger.info(String.format("Project Plan: %s", projectPlan.getName()));
                logger.info(String.format("Total Duration: %s  days", totalDuration));
                logger.info(String.format("Project Start: %1$s, End: %2$s", projectStart, projectEnd));

            // Output each task in the project plan
            projectPlan.getTasks().forEach(task -> {
                LocalDate[] taskDates = projectPlanService.calculateTaskDates(task, LocalDate.now());
                String taskStart = taskDates[0].format(formatter);
                String taskEnd = taskDates[1].format(formatter);
                logger.info(String.format("  Task: %1$s -> Start: %2$s, End: %3$s", task.getName(), taskStart, taskEnd));
            });

        }
    }
}