package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
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

    public ProjectPlanService(ProjectPlanRepository projectPlanRepository, TaskRepository taskRepository) {
        this.projectPlanRepository = projectPlanRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional
    public ProjectPlan createProjectPlan(ProjectPlanDTO projectPlanDTO) {
        List<Task> tasks = new ArrayList<>();
        ProjectPlan projectPlan = new ProjectPlan(projectPlanDTO.getName(), tasks);
        projectPlan = projectPlanRepository.save(projectPlan);

        for (TaskDTO taskDTO : projectPlanDTO.getTasks()) {
            // Dynamically resolve dependencies
            List<Task> dependencies = new ArrayList<>();
            for (TaskDTO depTask : taskDTO.getDependencies()) {
                Task dependency = taskRepository.findByName(depTask.getName());
                if (dependency != null) {
                    dependencies.add(dependency);
                }
            }

            Task task = new Task(taskDTO.getName(), taskDTO.getDuration(), dependencies, projectPlan);
            tasks.add(task);
            taskRepository.save(task);
        }

        projectPlan.setTasks(tasks);
        return projectPlan;
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

    private LocalDate[] calculateTaskDates(Task task) {
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
}
