package com.exist.scheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class ProjectPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "projectPlan", fetch = FetchType.EAGER)
    private List<Task> tasks;

    private LocalDate projectStartDate;
    private LocalDate projectEndDate;
    private long projectDuration;
    private long totalWorkingDays;

    // Constructors
    public ProjectPlan() {
    }

    public ProjectPlan(String name, LocalDate projectStartDate, List<Task> tasks) {
        this.name = name;
        this.projectStartDate = projectStartDate;
        this.tasks = tasks;
    }
}
