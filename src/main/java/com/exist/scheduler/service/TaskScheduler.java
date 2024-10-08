package com.exist.scheduler.service;

import com.exist.scheduler.dto.TaskDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskScheduler {

    public Map<TaskDTO, LocalDate[]> calculateSchedule(List<TaskDTO> tasks) {
        Map<TaskDTO, LocalDate[]> taskSchedule = new HashMap<>();

        // Iterate over the tasks and determine their start and end dates
        for (TaskDTO task : tasks) {
            LocalDate startDate;

            // If the task has dependencies, calculate its start date based on its dependencies
            if (task.isHasDependencies() && task.getDependencies() != null && !task.getDependencies().isEmpty()) {
                startDate = calculateStartDate(task, taskSchedule);
            } else {
                // If the task has no dependencies, it can start immediately
                startDate = LocalDate.now();
            }

            LocalDate endDate = startDate.plusDays(task.getDuration());
            taskSchedule.put(task, new LocalDate[]{startDate, endDate});
        }

        return taskSchedule;
    }

    // Calculate the start date based on dependencies
    private LocalDate calculateStartDate(TaskDTO task, Map<TaskDTO, LocalDate[]> taskSchedule) {
        LocalDate latestDependencyEnd = LocalDate.now(); // Start with current date

        // Iterate over all dependencies to find the latest end date
        for (TaskDTO dependency : task.getDependencies()) {
            LocalDate[] dependencyDates = taskSchedule.get(dependency);
            if (dependencyDates != null && dependencyDates[1].isAfter(latestDependencyEnd)) {
                latestDependencyEnd = dependencyDates[1]; // Update the latest dependency end date
            }
        }

        return latestDependencyEnd.plusDays(1); // The task can start one day after the last dependency finishes
    }
}