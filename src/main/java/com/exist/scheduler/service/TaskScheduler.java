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
        for (TaskDTO task : tasks) {
            LocalDate startDate = calculateStartDate(task, taskSchedule);
            LocalDate endDate = startDate.plusDays(task.getDuration());
            taskSchedule.put(task, new LocalDate[]{startDate, endDate});
        }
        return taskSchedule;
    }

    private LocalDate calculateStartDate(TaskDTO task, Map<TaskDTO, LocalDate[]> taskSchedule) {
        LocalDate latestDependencyEnd = LocalDate.now();

        for (TaskDTO dependency : task.getDependencies()) {
            LocalDate[] dependencyDates = taskSchedule.get(dependency);
            if (dependencyDates != null && dependencyDates[1].isAfter(latestDependencyEnd)) {
                latestDependencyEnd = dependencyDates[1];
            }
        }

        return latestDependencyEnd.plusDays(1); // Task starts the day after the latest dependency ends
    }
}
