package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.mapper.ProjectPlanMapper;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.ProjectPlanDetails;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.model.TaskDetails;
import com.exist.scheduler.repository.ProjectPlanRepository;
import com.exist.scheduler.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ProjectPlanService {

    @Autowired
    private ProjectPlanRepository projectPlanRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectPlanMapper projectPlanMapper;

    @Transactional
    public ProjectPlanDTO createProjectPlan(ProjectPlanDTO projectPlanDTO) {
        ProjectPlan projectPlan = projectPlanMapper.toEntity(projectPlanDTO);

        projectPlan = projectPlanRepository.save(projectPlan);

        // Recalculate task and project dates
        if (Optional.ofNullable(projectPlan.getTasks()).isPresent()) {
            recalculateTaskAndProjectDates(projectPlan);
        }

        return projectPlanMapper.toDTO(projectPlan);  // Return the saved project with tasks
    }

    @Transactional
    public void addTaskToProjectPlan(TaskDTO taskDTO) {

        ProjectPlan projectPlan = projectPlanRepository.findById(taskDTO.getProjectPlanId())
                .orElseThrow(() -> new NoSuchElementException(String.format("Project Plan with ID: %s not found", taskDTO.getProjectPlanId())));

        Task task = projectPlanMapper.toTaskEntity(taskDTO, projectPlan);
        projectPlan.getTasks().add(task);
        taskRepository.save(task);
    }

    public LocalDate[] calculateProjectDates(ProjectPlan projectPlan) {
        // Use the project's start date if provided, otherwise use the current date
        LocalDate projectStart = projectPlan.getProjectStartDate() != null
                ? projectPlan.getProjectStartDate()
                : LocalDate.now();

        LocalDate earliestTaskStart = projectStart;
        LocalDate latestTaskEnd = projectStart;

        for (Task task : projectPlan.getTasks()) {
            // Calculate task start and end dates
            LocalDate[] taskDates = calculateTaskDates(task, projectStart);

            if (taskDates[0].isBefore(earliestTaskStart)) {
                earliestTaskStart = taskDates[0];
            }

            if (taskDates[1].isAfter(latestTaskEnd)) {
                latestTaskEnd = taskDates[1];
            }
        }

        // Set the project's start and end dates to the calculated values
        projectPlan.setProjectStartDate(earliestTaskStart);
        projectPlan.setProjectEndDate(latestTaskEnd);

        projectPlan.setProjectDuration(ChronoUnit.DAYS.between(earliestTaskStart, latestTaskEnd));

        // Return the calculated start and end dates
        return new LocalDate[]{earliestTaskStart, latestTaskEnd};
    }


    public LocalDate[] calculateTaskDates(Task task, LocalDate projectStartDate) {
        LocalDate startDate = projectStartDate != null
                ? projectStartDate
                : LocalDate.now();

        // Calculate the start date based on dependencies
        for (Task dependency : task.getDependencies()) {
            LocalDate[] dependencyDates = calculateTaskDates(dependency, projectStartDate);
            if (dependencyDates[1].isAfter(startDate)) {
                // Start the current task the day after the latest dependency ends
                startDate = dependencyDates[1].plusDays(1);
            }
        }

        // Calculate end date by adding the task's duration and skipping weekends
        LocalDate endDate = addWorkingDays(startDate, task.getDuration());

        return new LocalDate[]{startDate, endDate};
    }


    public List<ProjectPlan> getAllProjectPlans() {
        return projectPlanRepository.findAll();
    }

    public ProjectPlanDTO getProjectPlanDTO(Long projectId) {
        ProjectPlan projectPlan = projectPlanRepository.findById(projectId).orElseThrow();
        return projectPlanMapper.toDTO(projectPlan);
    }

    public List<ProjectPlanDetails> toProjectPlanDetails() {
        List<ProjectPlan> projectPlans = getAllProjectPlans();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        List<ProjectPlanDetails> projectPlanDetailsList = new ArrayList<>();

        for (ProjectPlan projectPlan : projectPlans) {
            ProjectPlanDetails planDetails = new ProjectPlanDetails();
            LocalDate[] projectDates = calculateProjectDates(projectPlan);
            String projectStart = projectDates[0].format(formatter);
            String projectEnd = projectDates[1].format(formatter);

            long totalDuration = ChronoUnit.DAYS.between(projectDates[0], projectDates[1]);

            planDetails.setProjectPlanName(projectPlan.getName());
            planDetails.setProjectId(projectPlan.getId());
            planDetails.setTotalDuration(totalDuration);
            planDetails.setProjectStart(projectStart);
            planDetails.setProjectEnd(projectEnd);

            List<TaskDetails> taskDetailsList = new ArrayList<>();
            for (Task task : projectPlan.getTasks()) {
                TaskDetails taskDetails = new TaskDetails();
                LocalDate[] taskDates = calculateTaskDates(task, projectDates[0]);
                String taskStart = taskDates[0].format(formatter);
                String taskEnd = taskDates[1].format(formatter);

                taskDetails.setTaskName(task.getName());
                taskDetails.setTaskId(task.getId());
                taskDetails.setDuration(task.getDuration());
                taskDetails.setStartDate(taskStart);
                taskDetails.setEndDate(taskEnd);

                // Get the dependencies directly from the task, no need to query
                List<String> dependencies = task.getDependencies().stream()
                        .map(Task::getName)
                        .toList();

                taskDetails.setDependencies(dependencies);
                taskDetailsList.add(taskDetails);
            }
            planDetails.setTasks(taskDetailsList);
            projectPlanDetailsList.add(planDetails);
        }
        return projectPlanDetailsList;
    }

    public LocalDate addWorkingDays(LocalDate startDate, int daysToAdd) {
        LocalDate resultDate = startDate;
        int addedDays = 0;
        // The start date is counted as the first working day
        if (!(resultDate.getDayOfWeek() == DayOfWeek.SATURDAY || resultDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
            addedDays++;  // Count the start date as the first working day
        }

        while (addedDays < daysToAdd) {
            resultDate = resultDate.plusDays(1);

            if (resultDate.getDayOfWeek() != DayOfWeek.SATURDAY && resultDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }
        return resultDate;  // Return the last working day as the end date
    }

    @Transactional
    public void updateTask(Long taskId, TaskDTO taskDTO) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NoSuchElementException(String.format("Task with ID: %s not found", taskId)));

        // Update task properties
        task.setName(taskDTO.getName());
        task.setDuration(taskDTO.getDuration());

        // Update task dependencies
        List<Task> dependencies = new ArrayList<>();
        for (Long depId : taskDTO.getDependencies()) {
            Task dependency = taskRepository.findById(depId)
                    .orElseThrow(() -> new NoSuchElementException(String.format("Task with ID: %s not found", depId)));
            dependencies.add(dependency);
        }
        task.setDependencies(dependencies);

        // Recalculate the start and end dates
        recalculateTaskAndProjectDates(task.getProjectPlan());

        taskRepository.save(task);  // Save updated task
    }

    @Transactional
    public void updateProject(Long projectId, ProjectPlanDTO projectPlanDTO) {
        ProjectPlan projectPlan = projectPlanRepository.findById(projectId)
                .orElseThrow(() -> new NoSuchElementException("Project Plan with ID: " + projectId + " not found"));

        // Update project properties
        projectPlan.setName(projectPlanDTO.getName());
        projectPlan.setProjectStartDate(projectPlanDTO.getProjectStartDate());

        // Recalculate project and task dates
        recalculateTaskAndProjectDates(projectPlan);

        projectPlanRepository.save(projectPlan);  // Save updated project
    }

    public void recalculateTaskAndProjectDates(ProjectPlan projectPlan) {
        LocalDate earliestStartDate = projectPlan.getProjectStartDate();
        LocalDate latestEndDate = projectPlan.getProjectStartDate();

        for (Task task : projectPlan.getTasks()) {
            LocalDate[] taskDates = calculateTaskDates(task, projectPlan.getProjectStartDate());

            task.setTaskStartDate(taskDates[0]);
            task.setTaskEndDate(taskDates[1]);

            if (taskDates[0].isBefore(earliestStartDate)) {
                earliestStartDate = taskDates[0];
            }

            if (taskDates[1].isAfter(latestEndDate)) {
                latestEndDate = taskDates[1];
            }
        }

        projectPlan.setProjectStartDate(earliestStartDate);
        projectPlan.setProjectEndDate(latestEndDate);
        projectPlan.setProjectDuration(
                earliestStartDate == null || latestEndDate == null ? 0 : Math.abs(ChronoUnit.DAYS.between(earliestStartDate, latestEndDate)));
    }

    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new EntityNotFoundException("Task not found"));
        ProjectPlan projectPlan = task.getProjectPlan();
        projectPlan.getTasks().remove(task);
        taskRepository.delete(task);
        projectPlanRepository.save(projectPlan);
    }

    public void deleteProject(Long projectId) {
        ProjectPlan projectPlan = projectPlanRepository.findById(projectId).orElseThrow(() -> new EntityNotFoundException("Project plan not found"));
        projectPlanRepository.delete(projectPlan);
    }
}