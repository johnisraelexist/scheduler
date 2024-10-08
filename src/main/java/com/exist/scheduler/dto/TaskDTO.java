package com.exist.scheduler.dto;

import com.exist.scheduler.model.ProjectPlan;
import lombok.Data;

import java.util.List;

@Data
public class TaskDTO {

    private Long id;
    private String name;
    private int duration;
    private List<TaskDTO> dependencies;
    private ProjectPlan projectPlan;

    public TaskDTO() {
    }

    public TaskDTO(Long id, String name, int duration, List<TaskDTO> dependencies, ProjectPlan projectPlan) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.dependencies = dependencies;
        this.projectPlan = projectPlan;
    }
}
