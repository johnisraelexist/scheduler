package com.exist.scheduler.controller;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlanResponse;
import com.exist.scheduler.service.ProjectPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/projects")
public class ProjectPlanController {

    private final ProjectPlanService projectPlanService;

    public ProjectPlanController(ProjectPlanService projectPlanService) {
        this.projectPlanService = projectPlanService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createProjectPlan(@RequestBody ProjectPlanDTO projectPlanDTO) {
        try {
            ProjectPlanDTO savedProject = projectPlanService.createProjectPlan(projectPlanDTO);
            return ResponseEntity.ok(String.format("Project plan created with ID: %s", savedProject.getId()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/add-task")
    public ResponseEntity<String> addTaskToProjectPlan(@RequestBody TaskDTO taskDTO) {
        try {
            projectPlanService.addTaskToProjectPlan(taskDTO);
            return ResponseEntity.ok("Task added to project plan with ID: " + taskDTO.getProjectPlanId());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/retrieve-all")
    public ResponseEntity<ProjectPlanResponse> retrieveAllProjectPlan(){
        ProjectPlanResponse projectPlanResponse = new ProjectPlanResponse(("All Project Plans"), projectPlanService.toProjectPlanDetails());
        return ResponseEntity.ok(projectPlanResponse);
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<String> updateTask(@PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        try {
        projectPlanService.updateTask(taskId, taskDTO);
        return ResponseEntity.ok("Task updated and affected dates recalculated.");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody ProjectPlanDTO projectPlanDTO) {
        projectPlanService.updateProject(projectId, projectPlanDTO);
        return ResponseEntity.ok("Project updated and affected dates recalculated.");
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        projectPlanService.deleteTask(taskId);
        return ResponseEntity.ok("Task deleted.");
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectPlanService.deleteProject(projectId);
        return ResponseEntity.ok("Project deleted.");
    }
}
