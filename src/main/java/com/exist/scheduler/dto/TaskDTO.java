package com.exist.scheduler.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String name;
    private int duration;
    private List<Long> dependencies;
    private Long projectPlanId;

    private LocalDate taskStartDate;
    private LocalDate taskEndDate;

    public TaskDTO() {}

    public TaskDTO(Long id, String name, int duration, List<Long> dependencies, Long projectPlanId, LocalDate taskStartDate, LocalDate taskEndDate) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.dependencies = dependencies;
        this.projectPlanId = projectPlanId;
        this.taskStartDate = taskStartDate;
        this.taskEndDate = taskEndDate;
    }
}
