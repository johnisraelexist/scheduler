package com.exist.scheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectPlanDetails {
    private String projectPlanName;
    private long projectId;
    private long totalDuration;
    private String projectStart;
    private String projectEnd;
    private List<TaskDetails> tasks;
}
