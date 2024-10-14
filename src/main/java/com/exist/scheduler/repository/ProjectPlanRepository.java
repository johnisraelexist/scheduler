package com.exist.scheduler.repository;

import com.exist.scheduler.model.ProjectPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectPlanRepository extends JpaRepository<ProjectPlan, Long> {
}