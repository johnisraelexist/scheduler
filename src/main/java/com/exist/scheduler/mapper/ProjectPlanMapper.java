package com.exist.scheduler.mapper;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.ProjectPlanRepository;
import com.exist.scheduler.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class ProjectPlanMapper {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectPlanRepository projectPlanRepository;

    // Convert ProjectPlanDTO to ProjectPlan entity
    public ProjectPlan toProjectPlanEntity(ProjectPlanDTO projectPlanDTO) {
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setName(projectPlanDTO.getName());
        LocalDate projectStart = projectPlanDTO.getProjectStartDate() != null
                ? projectPlanDTO.getProjectStartDate()
                : LocalDate.now();
        projectPlan.setProjectStartDate(projectStart);

        projectPlan = projectPlanRepository.save(projectPlan);

        if (projectPlanDTO.getTasks() != null && !projectPlanDTO.getTasks().isEmpty()) {
            if (projectPlan.getTasks() == null) {
                projectPlan.setTasks(new ArrayList<>());  // Initialize the task list if null
            }
            for (TaskDTO taskDTO : projectPlanDTO.getTasks()) {
                taskDTO.setProjectPlanId(projectPlan.getId());
                Task task = toTaskEntity(taskDTO, projectPlan);
                taskRepository.save(task);
                projectPlan.getTasks().add(task);
            }
        }

        return projectPlan;
    }

    // Convert TaskDTO to Task entity
    public Task toTaskEntity(TaskDTO taskDTO, ProjectPlan projectPlan) {
        List<Task> dependencies = toTaskDependencies(taskDTO, projectPlan);

        return new Task(taskDTO.getName(), taskDTO.getDuration(), dependencies, projectPlan);
    }

    public List<Task> toTaskDependencies (TaskDTO taskDTO, ProjectPlan projectPlan){
        List<Task> dependencies = new ArrayList<>();

        for (Long depId : taskDTO.getDependencies()) {
            Task dependency = taskRepository.findById(depId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format("Task with ID: %s not found", depId)));

            Long dependencyProjectId = Optional.ofNullable(dependency.getProjectPlan())
                    .map(ProjectPlan::getId)
                    .orElseThrow(() -> new NoSuchElementException(
                            String.format("Task with ID: %s has no project plan", depId)));

            Long currentProjectId = Optional.ofNullable(projectPlan.getId())
                    .orElseThrow(() -> new NoSuchElementException("Current task's project plan has no ID"));

            if (!dependencyProjectId.equals(currentProjectId)) {
                throw new NoSuchElementException(
                        String.format("Task with ID: %s belongs to a different project", depId));
            }

            dependencies.add(dependency);
        }

        return dependencies;
    }

    // Convert ProjectPlan entity to ProjectPlanDTO
    public ProjectPlanDTO toProjectPlanDTO(ProjectPlan projectPlan) {
        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setId(projectPlan.getId());
        projectPlanDTO.setName(projectPlan.getName());
        projectPlanDTO.setProjectDuration(projectPlan.getProjectDuration());
        projectPlanDTO.setProjectStartDate(projectPlan.getProjectStartDate());
        projectPlanDTO.setProjectEndDate(projectPlan.getProjectEndDate());

        // Convert Tasks to TaskDTOs
        if(projectPlan.getTasks() != null && !projectPlan.getTasks().isEmpty()) {
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