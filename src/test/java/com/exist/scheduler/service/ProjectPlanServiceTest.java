package com.exist.scheduler.service;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.mapper.ProjectPlanMapper;
import com.exist.scheduler.model.ProjectPlan;
import com.exist.scheduler.model.ProjectPlanDetails;
import com.exist.scheduler.model.Task;
import com.exist.scheduler.repository.ProjectPlanRepository;
import com.exist.scheduler.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProjectPlanServiceTest {

    @Mock
    private ProjectPlanRepository projectPlanRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectPlanMapper projectPlanMapper;

    @InjectMocks
    private ProjectPlanService projectPlanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test for creating a project plan with tasks
    @Test
    void createProjectPlan_WithTasks() {
        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setName("New Project");
        projectPlanDTO.setTasks(new ArrayList<>());

        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setId(1L);

        when(projectPlanMapper.toEntity(any(ProjectPlanDTO.class))).thenReturn(projectPlan);
        when(projectPlanRepository.save(any(ProjectPlan.class))).thenReturn(projectPlan);
        when(projectPlanMapper.toDTO(any(ProjectPlan.class))).thenReturn(projectPlanDTO);

        ProjectPlanDTO savedProjectPlanDTO = projectPlanService.createProjectPlan(projectPlanDTO);

        assertNotNull(savedProjectPlanDTO);
        assertEquals("New Project", savedProjectPlanDTO.getName());
        verify(projectPlanRepository, times(1)).save(any(ProjectPlan.class));
    }

    // Test for adding a task to an existing project plan
    @Test
    void addTaskToProjectPlan_Success() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setProjectPlanId(1L);
        taskDTO.setName("Task 1");

        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setId(1L);
        projectPlan.setTasks(new ArrayList<>());

        Task task = new Task();
        task.setName("Task 1");

        when(projectPlanRepository.findById(1L)).thenReturn(Optional.of(projectPlan));
        when(projectPlanMapper.toTaskEntity(any(TaskDTO.class), any())).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        projectPlanService.addTaskToProjectPlan(taskDTO);
        assertNotNull(projectPlanService.getAllProjectPlans());
    }

    // Test for adding a task to a project plan that does not exist
    @Test
    void addTaskToProjectPlan_ProjectNotFound() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setProjectPlanId(1L);

        when(projectPlanRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            projectPlanService.addTaskToProjectPlan(taskDTO);
        });

        assertEquals("Project Plan with ID: 1 not found", exception.getMessage());
        verify(projectPlanRepository, times(1)).findById(1L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // Test for calculating project dates with tasks
    @Test
    void calculateProjectDates() {
        // Mock project with tasks
        ProjectPlan projectPlan = new ProjectPlan();
        Task task1 = new Task();
        task1.setDuration(5);
        List<Task> tasks1 = new ArrayList<>();
        task1.setDependencies(tasks1);
        Task task2 = new Task();
        task2.setDuration(3);
        List<Task> tasks2 = new ArrayList<>();
        task2.setDependencies(tasks2);
        projectPlan.setTasks(List.of(task1, task2));

        // Call the method
        LocalDate[] projectDates = projectPlanService.calculateProjectDates(projectPlan);

        // Assertions
        assertNotNull(projectDates);
        assertTrue(projectDates[1].isAfter(projectDates[0]));
    }

    // Test for calculating working days, skipping weekends
    @Test
    void addWorkingDays() {
        LocalDate startDate = LocalDate.of(2024, 10, 9); // Wednesday
        LocalDate endDate = projectPlanService.addWorkingDays(startDate, 5); // Adding 5 working days

        assertEquals(LocalDate.of(2024, 10, 15), endDate); // End date should be October 15, 2024
    }

    // Test for converting ProjectPlan to ProjectPlanDetails
    @Test
    void toProjectPlanDetails() {
        // Mock project plan
        ProjectPlan projectPlan = new ProjectPlan();
        Task task = new Task();
        task.setDuration(5);
        List<Task> tasks1 = new ArrayList<>();
        task.setDependencies(tasks1);
        projectPlan.setTasks(List.of(task));
        projectPlan.setName("Test Project");
        projectPlan.setId(1L);
        when(projectPlanRepository.findAll()).thenReturn(List.of(projectPlan));

        // Mock task details if needed
        when(projectPlanMapper.toDTO(any())).thenReturn(new ProjectPlanDTO());

        // Call the method
        List<ProjectPlanDetails> details = projectPlanService.toProjectPlanDetails();

        // Assertions
        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals("Test Project", details.get(0).getProjectPlanName());
    }
}
