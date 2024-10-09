package com.exist.scheduler.mapper;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.TaskRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class ProjectPlanMapper {

    private final TaskRepository taskRepository;

    public ProjectPlanMapper(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Convert ProjectPlanDTO to ProjectPlan entity
    public ProjectPlan toEntity(ProjectPlanDTO projectPlanDTO) {
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setName(projectPlanDTO.getName());
        return projectPlan;
    }

    // Convert TaskDTO to Task entity
    public Task toTaskEntity(TaskDTO taskDTO, ProjectPlan projectPlan) {
        List<Task> dependencies = new ArrayList<>();

        // Convert task dependency IDs to Task entities
        for (Long depId : taskDTO.getDependencies()) {
            // Find each dependency by its ID from the repository
            Task dependency = taskRepository.findById(depId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format("Task with ID: %s not found", depId)));
            dependencies.add(dependency);
        }

        // Create new Task with dependencies and project plan reference
        return new Task(taskDTO.getName(), taskDTO.getDuration(), dependencies, projectPlan);
    }

    // Convert ProjectPlan entity to ProjectPlanDTO
    public ProjectPlanDTO toDTO(ProjectPlan projectPlan) {
        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setId(projectPlan.getId());
        projectPlanDTO.setName(projectPlan.getName());

        // Convert Tasks to TaskDTOs
        if(projectPlanDTO.getTasks() != null && !projectPlanDTO.getTasks().isEmpty()) {
            List<TaskDTO> taskDTOs = new ArrayList<>();
            for (Task task : projectPlan.getTasks()) {
                taskDTOs.add(toTaskDTO(task));
            }
            projectPlanDTO.setTasks(taskDTOs);
        }

        return projectPlanDTO;
    }

    // Convert Task entity to TaskDTO
    public TaskDTO toTaskDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(task.getId());
        taskDTO.setName(task.getName());
        taskDTO.setDuration(task.getDuration());

        // Convert Task dependencies to a list of IDs (no recursive TaskDTO conversion)
        List<Long> dependencyIds = new ArrayList<>();
        for (Task dependency : task.getDependencies()) {
            dependencyIds.add(dependency.getId());  // Add the dependency ID instead of converting to TaskDTO
        }
        taskDTO.setDependencies(dependencyIds);  // Set the dependencies as a list of IDs
        taskDTO.setProjectPlanId(task.getProjectPlan().getId());

        return taskDTO;
    }
}
