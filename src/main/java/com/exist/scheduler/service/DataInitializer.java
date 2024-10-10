package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        List<TaskDTO> taskDTOList = new ArrayList<>();
        // Create tasks for the first project using the projectPlanId
        TaskDTO designTask = new TaskDTO(null, "Design", 5, List.of(), null, null, null);
        TaskDTO developmentTask = new TaskDTO(null, "Development", 10, List.of(1L), null, null, null);
        TaskDTO testingTask = new TaskDTO(null, "Testing", 3, List.of(2L), null, null, null);
        taskDTOList.add(designTask);
        taskDTOList.add(developmentTask);
        taskDTOList.add(testingTask);
        websiteProject.setTasks(taskDTOList);
        projectPlanService.createProjectPlan(websiteProject);

        //Create second Project Plan: Mobile App Development
        ProjectPlanDTO mobileAppProject = new ProjectPlanDTO();
        mobileAppProject.setName("Mobile App Development");

        List<TaskDTO> taskDTOList1 = new ArrayList<>();

        // Create tasks for the second project
        TaskDTO planningTask = new TaskDTO(null, "Planning", 4, List.of(), null, null, null);
        TaskDTO codingTask = new TaskDTO(null, "Coding", 12, List.of(4L), null, null, null);
        TaskDTO reviewTask = new TaskDTO(null, "Review", 2, List.of(5L), null, null, null);

        taskDTOList1.add(planningTask);
        taskDTOList1.add(codingTask);
        taskDTOList1.add(reviewTask);
        mobileAppProject.setTasks(taskDTOList1);
        projectPlanService.createProjectPlan(mobileAppProject);
    }
}