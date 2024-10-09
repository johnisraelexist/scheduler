package com.exist.scheduler.controller;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.service.ProjectPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectPlanController {

    private final ProjectPlanService projectPlanService;

    public ProjectPlanController(ProjectPlanService projectPlanService) {
        this.projectPlanService = projectPlanService;
    }

    // Endpoint to create a new Project Plan with optional tasks
    @PostMapping("/create")
    public ResponseEntity<String> createProjectPlan(@RequestBody ProjectPlanDTO projectPlanDTO) {
        ProjectPlanDTO savedProject = projectPlanService.createProjectPlan(projectPlanDTO);
        return ResponseEntity.ok("Project plan created with ID: " + savedProject.getId());
    }

    // Endpoint to add a task to a project plan (if required separately)
    @PostMapping("/add-task")
    public ResponseEntity<String> addTaskToProjectPlan(@RequestBody TaskDTO taskDTO) {
        projectPlanService.addTaskToProjectPlan(taskDTO);
        return ResponseEntity.ok("Task added to project plan with ID: " + taskDTO.getProjectPlanId());
    }
}
