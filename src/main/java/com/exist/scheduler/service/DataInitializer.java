package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer {

    private final ProjectPlanService projectPlanService;

    public DataInitializer(ProjectPlanService projectPlanService) {
        this.projectPlanService = projectPlanService;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        // Create first Project Plan: Website Development
        ProjectPlanDTO websiteProject = new ProjectPlanDTO();
        websiteProject.setName("Website Development");

        // Save project to get the ID for tasks
        ProjectPlanDTO savedWebsiteProject = projectPlanService.createProjectPlan(websiteProject);

        // Create tasks for the first project using the projectPlanId
        TaskDTO designTask = new TaskDTO(null, "Design", 5, Arrays.asList(), savedWebsiteProject.getId());
        TaskDTO developmentTask = new TaskDTO(null, "Development", 10, Arrays.asList(designTask), savedWebsiteProject.getId());
        TaskDTO testingTask = new TaskDTO(null, "Testing", 3, Arrays.asList(developmentTask), savedWebsiteProject.getId());

        projectPlanService.addTaskToProjectPlan(designTask);
        projectPlanService.addTaskToProjectPlan(developmentTask);
        projectPlanService.addTaskToProjectPlan(testingTask);

        // Create second Project Plan: Mobile App Development
        ProjectPlanDTO mobileAppProject = new ProjectPlanDTO();
        mobileAppProject.setName("Mobile App Development");

        // Save project to get the ID for tasks
        ProjectPlanDTO savedMobileAppProject = projectPlanService.createProjectPlan(mobileAppProject);

        // Create tasks for the second project
        TaskDTO planningTask = new TaskDTO(null, "Planning", 4, Arrays.asList(), savedMobileAppProject.getId());
        TaskDTO codingTask = new TaskDTO(null, "Coding", 12, Arrays.asList(planningTask), savedMobileAppProject.getId());
        TaskDTO reviewTask = new TaskDTO(null, "Review", 2, Arrays.asList(codingTask), savedMobileAppProject.getId());

        projectPlanService.addTaskToProjectPlan(planningTask);
        projectPlanService.addTaskToProjectPlan(codingTask);
        projectPlanService.addTaskToProjectPlan(reviewTask);
    }
}