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

        return projectPlanMapper.toProjectPlanDTO(projectPlan);  // Return the saved project with tasks
    }

    @Transactional
    public void addTaskToProjectPlan(TaskDTO taskDTO) {

        ProjectPlan projectPlan = projectPlanRepository.findById(taskDTO.getProjectPlanId())
                .orElseThrow(() -> new NoSuchElementException(String.format("Project Plan with ID: %s not found", taskDTO.getProjectPlanId())));

        Task task = projectPlanMapper.toTaskEntity(taskDTO, projectPlan);
        projectPlan.getTasks().add(task);
        taskRepository.save(task);
        recalculateTaskAndProjectDates(projectPlan);
    }

    public LocalDate[] calculateProjectDates(ProjectPlan projectPlan) {
        // Use the project's start date if provided, otherwise use the current date
        LocalDate projectStart = projectPlan.getProjectStartDate() != null
                ? projectPlan.getProjectStartDate()
                : LocalDate.now();

        LocalDate earliestTaskStart = projectStart;
        LocalDate latestTaskEnd = projectStart;

        for (Task task : projectPlan.getTasks()) {
            LocalDate[] taskDates = calculateTaskDates(task, projectStart);

            if (taskDates[0].isBefore(earliestTaskStart)) {
                earliestTaskStart = taskDates[0];
            }

            if (taskDates[1].isAfter(latestTaskEnd)) {
                latestTaskEnd = taskDates[1];
            }
        }

        projectPlan.setProjectStartDate(earliestTaskStart);
        projectPlan.setProjectEndDate(latestTaskEnd);

        // Compute the total number of days (including weekends)
        long totalDays = Math.abs(ChronoUnit.DAYS.between(earliestTaskStart, latestTaskEnd));

        projectPlan.setProjectDuration(totalDays);

        return new LocalDate[]{earliestTaskStart, latestTaskEnd};
    }


    public LocalDate[] calculateTaskDates(Task task, LocalDate projectStartDate) {
        LocalDate startDate = projectStartDate != null
                ? projectStartDate
                : LocalDate.now();

        // Calculate the start date based on dependencies
        if (Optional.ofNullable(task.getDependencies()).isPresent()) {
            for (Task dependency : task.getDependencies()) {
                LocalDate[] dependencyDates = calculateTaskDates(dependency, projectStartDate);
                if (dependencyDates[1].isAfter(startDate)) {
                    // Start the current task the day after the latest dependency ends
                    startDate = dependencyDates[1].plusDays(1);
                }
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
        return projectPlanMapper.toProjectPlanDTO(projectPlan);
    }

    public List<ProjectPlanDetails> toProjectPlanDetails() {
        List<ProjectPlan> projectPlans = getAllProjectPlans();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        List<ProjectPlanDetails> projectPlanDetailsList = new ArrayList<>();

        for (ProjectPlan projectPlan : projectPlans) {

            ProjectPlanDetails planDetails = new ProjectPlanDetails();

            planDetails.setProjectPlanName(projectPlan.getName());
            planDetails.setProjectId(projectPlan.getId());
            planDetails.setTotalDuration(projectPlan.getProjectDuration());
            planDetails.setTotalWorkingDays(projectPlan.getTotalWorkingDays());

            // Handle null projectStartDate and projectEndDate
            String projectStart = (projectPlan.getProjectStartDate() != null)
                    ? projectPlan.getProjectStartDate().format(formatter)
                    : "N/A";
            String projectEnd = (projectPlan.getProjectEndDate() != null)
                    ? projectPlan.getProjectEndDate().format(formatter)
                    : "N/A";

            planDetails.setProjectStart(projectStart);
            planDetails.setProjectEnd(projectEnd);

            List<TaskDetails> taskDetailsList = new ArrayList<>();
            for (Task task : projectPlan.getTasks()) {
                TaskDetails taskDetails = new TaskDetails();

                taskDetails.setTaskName(task.getName());
                taskDetails.setTaskId(task.getId());
                taskDetails.setDuration(task.getDuration());

                // Handle null taskStartDate and taskEndDate
                String taskStart = (task.getTaskStartDate() != null)
                        ? task.getTaskStartDate().format(formatter)
                        : "N/A";
                String taskEnd = (task.getTaskEndDate() != null)
                        ? task.getTaskEndDate().format(formatter)
                        : "N/A";

                taskDetails.setStartDate(taskStart);
                taskDetails.setEndDate(taskEnd);

                // Handle dependencies
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
        LocalDate earliestStartDate = Optional.ofNullable(projectPlan.getProjectStartDate())
                .orElse(LocalDate.now());

        LocalDate latestEndDate = Optional.ofNullable(projectPlan.getProjectStartDate())
                .orElse(LocalDate.now());

        for (Task task : projectPlan.getTasks()) {
            LocalDate[] taskDates = calculateTaskDates(task, projectPlan.getProjectStartDate());

            // Use Optional to handle nulls in taskDates and check both start and end dates
            Optional<LocalDate> taskStartDate = Optional.ofNullable(taskDates[0]);
            Optional<LocalDate> taskEndDate = Optional.ofNullable(taskDates[1]);

            taskStartDate.ifPresent(task::setTaskStartDate);  // Set task start date if present
            taskEndDate.ifPresent(task::setTaskEndDate);  // Set task end date if present

            // Update the earliest start date
            LocalDate finalEarliestStartDate = earliestStartDate;
            earliestStartDate = taskStartDate
                    .filter(startDate -> startDate.isBefore(finalEarliestStartDate))  // Check if task start is earlier
                    .orElse(earliestStartDate);  // Keep current if not earlier

            // Update the latest end date
            LocalDate finalLatestEndDate = latestEndDate;
            latestEndDate = taskEndDate
                    .filter(endDate -> endDate.isAfter(finalLatestEndDate))  // Check if task end is later
                    .orElse(latestEndDate);  // Keep current if not later
        }

        // Compute the total number of working days (excluding weekends)
        long totalWorkingDays = calculateWorkingDays(earliestStartDate, latestEndDate);

        projectPlan.setTotalWorkingDays(totalWorkingDays);
        projectPlan.setProjectStartDate(earliestStartDate);
        projectPlan.setProjectEndDate(latestEndDate);
        projectPlan.setProjectDuration(Math.abs(ChronoUnit.DAYS.between(earliestStartDate, latestEndDate)));
    }

    public long calculateWorkingDays(LocalDate start, LocalDate end) {
        long workingDays = 0;
        LocalDate currentDate = start;

        while (!currentDate.isAfter(end)) {
            // Count only weekdays (Monday to Friday)
            if (currentDate.getDayOfWeek() != DayOfWeek.SATURDAY
                    && currentDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return workingDays;
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