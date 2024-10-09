package com.exist.scheduler.mapper;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.TaskRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

        // Convert task dependencies to Task entities
        for (TaskDTO depDTO : taskDTO.getDependencies()) {
            Task dependency = taskRepository.findByName(depDTO.getName());
            if (dependency != null) {
                dependencies.add(dependency);
            }
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

        // Convert Task dependencies to TaskDTOs
        List<TaskDTO> dependencyDTOs = new ArrayList<>();
        for (Task dependency : task.getDependencies()) {
            dependencyDTOs.add(toTaskDTO(dependency));  // Recursive conversion for dependencies
        }
        taskDTO.setDependencies(dependencyDTOs);
        taskDTO.setProjectPlanId(task.getProjectPlan().getId());
        return taskDTO;
    }
}
