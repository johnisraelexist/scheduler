package com.exist.scheduler.dto;

import lombok.Data;

import java.util.List;

@Data
public class TaskDTO {

    private Long id;
    private String name;
    private int duration;
    private List<TaskDTO> dependencies;
    private Long projectPlanId;  // Only include the ID of the project plan

    public TaskDTO() {
    }

    public TaskDTO(Long id, String name, int duration, List<TaskDTO> dependencies, Long projectPlanId) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.dependencies = dependencies;
        this.projectPlanId = projectPlanId;
    }
}
