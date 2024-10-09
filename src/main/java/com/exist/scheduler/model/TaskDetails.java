package com.exist.scheduler.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskDetails {
    private String taskName;
    private int duration;
    private String startDate;
    private String endDate;
    private List<String> dependencies;
}
