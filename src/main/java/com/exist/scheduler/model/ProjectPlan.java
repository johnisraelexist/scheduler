package com.exist.scheduler.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class ProjectPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "projectPlan", fetch = FetchType.LAZY)
    private List<Task> tasks;

    // Constructors
    public ProjectPlan() {
    }

    public ProjectPlan(String name, List<Task> tasks) {
        this.name = name;
        this.tasks = tasks;
    }
}
