package com.exist.scheduler.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectPlanDTO {
    private Long id;
    private String name;
    private List<TaskDTO> tasks;  // Optional tasks can be included during project creation

    public ProjectPlanDTO() {}

    public ProjectPlanDTO(Long id, String name, List<TaskDTO> tasks) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
    }
}
