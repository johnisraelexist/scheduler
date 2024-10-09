package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.mapper.ProjectPlanMapper;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.ProjectPlanRepository;
import com.exist.scheduler.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectPlanService {

    private final ProjectPlanRepository projectPlanRepository;
    private final TaskRepository taskRepository;
    private final ProjectPlanMapper projectPlanMapper;

    public ProjectPlanService(ProjectPlanRepository projectPlanRepository, TaskRepository taskRepository, ProjectPlanMapper projectPlanMapper) {
        this.projectPlanRepository = projectPlanRepository;
        this.taskRepository = taskRepository;
        this.projectPlanMapper = projectPlanMapper;
    }

    @Transactional
    public ProjectPlanDTO createProjectPlan(ProjectPlanDTO projectPlanDTO) {
        // Convert and save ProjectPlan
        ProjectPlan projectPlan = projectPlanMapper.toEntity(projectPlanDTO);
        projectPlan = projectPlanRepository.save(projectPlan);

        // Handle tasks in ProjectPlanDTO
        if (projectPlanDTO.getTasks() != null && !projectPlanDTO.getTasks().isEmpty()) {
            for (TaskDTO taskDTO : projectPlanDTO.getTasks()) {
                // Ensure the task is linked to the project by setting projectPlanId
                taskDTO.setProjectPlanId(projectPlan.getId());
                Task task = projectPlanMapper.toTaskEntity(taskDTO, projectPlan);
                taskRepository.save(task);
                if (projectPlan.getTasks() != null && projectPlan.getTasks().isEmpty()) {
                    projectPlan.getTasks().add(task); // Add the task to the project's task list
                } else {
                    List<Task> tasks = new ArrayList<>();
                    tasks.add(task);
                    projectPlan.setTasks(tasks);
                }
            }
        }

        return projectPlanMapper.toDTO(projectPlan);  // Return the saved project with tasks
    }

    @Transactional
    public void addTaskToProjectPlan(TaskDTO taskDTO) {
        ProjectPlan projectPlan = projectPlanRepository.findById(taskDTO.getProjectPlanId()).orElseThrow();
        Task task = projectPlanMapper.toTaskEntity(taskDTO, projectPlan);
        projectPlan.getTasks().add(task);
        taskRepository.save(task);
    }

    public LocalDate[] calculateProjectDates(ProjectPlan projectPlan) {
        LocalDate projectStart = LocalDate.now();
        LocalDate projectEnd = projectStart;

        for (Task task : projectPlan.getTasks()) {
            LocalDate[] taskDates = calculateTaskDates(task);
            if (taskDates[1].isAfter(projectEnd)) {
                projectEnd = taskDates[1];
            }
        }

        return new LocalDate[]{projectStart, projectEnd};
    }

    public LocalDate[] calculateTaskDates(Task task) {
        LocalDate startDate = LocalDate.now();

        for (Task dependency : task.getDependencies()) {
            LocalDate[] dependencyDates = calculateTaskDates(dependency);
            if (dependencyDates[1].isAfter(startDate)) {
                startDate = dependencyDates[1];
            }
        }

        LocalDate endDate = startDate.plusDays(task.getDuration());
        return new LocalDate[]{startDate, endDate};
    }

    public List<ProjectPlan> getAllProjectPlans() {
        return projectPlanRepository.findAll();
    }

    public ProjectPlanDTO getProjectPlanDTO(Long projectId) {
        ProjectPlan projectPlan = projectPlanRepository.findById(projectId).orElseThrow();
        return projectPlanMapper.toDTO(projectPlan);
    }
}