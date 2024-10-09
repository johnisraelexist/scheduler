package com.exist.scheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProjectPlanResponse {
    private String message;
    private List<ProjectPlanDetails> projectPlans = new ArrayList<>();

    public ProjectPlanResponse() {
    }

    public ProjectPlanResponse(String message, List<ProjectPlanDetails> projectPlans) {
        this.message = message;
        this.projectPlans = projectPlans;
    }
}
