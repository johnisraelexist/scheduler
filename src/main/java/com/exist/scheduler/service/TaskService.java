package com.exist.scheduler.service;

import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.mapper.TaskMapper;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void saveTasks(List<TaskDTO> taskDTOs) {
        List<Task> tasks = taskDTOs.stream()
                .map(taskMapper::toEntity)
                .collect(Collectors.toList());
        taskRepository.saveAll(tasks);
    }
}
