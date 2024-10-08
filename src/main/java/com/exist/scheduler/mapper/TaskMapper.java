package com.exist.scheduler.mapper;

import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.Task;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public TaskDTO toDTO(Task task) {
        List<TaskDTO> dependencies = task.getDependencies().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        ProjectPlan projectPlan = task.getProjectPlan();

        return new TaskDTO(task.getId(), task.getName(), task.getDuration(), dependencies, projectPlan);
    }

    public Task toEntity(TaskDTO taskDTO) {
        List<Task> dependencies = taskDTO.getDependencies().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        ProjectPlan projectPlan = taskDTO.getProjectPlan();

        return new Task(taskDTO.getName(), taskDTO.getDuration(), dependencies, projectPlan);
    }
}
