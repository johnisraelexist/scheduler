package com.exist.scheduler.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectPlanDTO {
    private String name;
    private List<TaskDTO> tasks;
}
