package com.exist.scheduler.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectPlanDTO {
    private Long id;
    private String name;
    private List<TaskDTO> tasks;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private long projectDuration;

    public ProjectPlanDTO() {}

    public ProjectPlanDTO(Long id, String name, List<TaskDTO> tasks, LocalDate projectStartDate, LocalDate projectEndDate, long projectDuration) {
        this.id = id;
        this.name = name;
        this.tasks = tasks;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.projectDuration = projectDuration;
    }
}
