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
        // Convert and save ProjectPlan
        ProjectPlan projectPlan = projectPlanMapper.toEntity(projectPlanDTO);
        projectPlan = projectPlanRepository.save(projectPlan);

        // Handle tasks in ProjectPlanDTO
        if (projectPlanDTO.getTasks() != null && !projectPlanDTO.getTasks().isEmpty()) {
            if (projectPlan.getTasks() == null) {
                projectPlan.setTasks(new ArrayList<>());  // Initialize the task list if null
            }
            for (TaskDTO taskDTO : projectPlanDTO.getTasks()) {
                // Ensure the task is linked to the project by setting projectPlanId
                taskDTO.setProjectPlanId(projectPlan.getId());
                Task task = projectPlanMapper.toTaskEntity(taskDTO, projectPlan);
                taskRepository.save(task);  // Save each task to the repository

                projectPlan.getTasks().add(task);  // Add each task to the project's task list
            }
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
        LocalDate projectStart = LocalDate.now();
        LocalDate projectEnd = projectStart;
        // Calculate the project end date based on the longest task duration
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

        // Calculate start date based on dependencies
        for (Task dependency : task.getDependencies()) {
            LocalDate[] dependencyDates = calculateTaskDates(dependency);
            if (dependencyDates[1].isAfter(startDate)) {
                startDate = dependencyDates[1];
            }
        }

        // Calculate end date, skipping weekends
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
                LocalDate[] taskDates = calculateTaskDates(task);
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
}