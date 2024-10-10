package com.exist.scheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int duration;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "task_dependencies",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "dependency_id")
    )
    private List<Task> dependencies;

    @ManyToOne
    @JoinColumn(name = "project_plan_id")
    private ProjectPlan projectPlan;

    private LocalDate taskStartDate;
    private LocalDate taskEndDate;

    // Constructors
    public Task() {}

    public Task(String name, int duration, List<Task> dependencies, ProjectPlan projectPlan) {
        this.name = name;
        this.duration = duration;
        this.dependencies = dependencies;
        this.projectPlan = projectPlan;
    }

}
