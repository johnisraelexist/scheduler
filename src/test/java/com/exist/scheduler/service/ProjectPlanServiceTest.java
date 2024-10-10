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
    void testCreateProjectPlan() {
        // Prepare mock data
        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setName("New Project");

        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setName("New Project");
        projectPlan.setTasks(new ArrayList<>());  // Initialize the tasks list to avoid NullPointerException

        when(projectPlanMapper.toEntity(projectPlanDTO)).thenReturn(projectPlan);
        when(projectPlanRepository.save(any(ProjectPlan.class))).thenReturn(projectPlan);
        when(projectPlanMapper.toProjectPlanDTO(any(ProjectPlan.class))).thenReturn(projectPlanDTO);

        // Call the method under test
        ProjectPlanDTO result = projectPlanService.createProjectPlan(projectPlanDTO);

        // Assertions
        assertEquals("New Project", result.getName());
        verify(projectPlanRepository, times(1)).save(any(ProjectPlan.class));
        verify(projectPlanMapper, times(1)).toEntity(any(ProjectPlanDTO.class));
        verify(projectPlanMapper, times(1)).toProjectPlanDTO(any(ProjectPlan.class));
    }

    // Test for adding a task to an existing project plan
    @Test
    void addTaskToProjectPlan_Success() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setProjectPlanId(1L);
        taskDTO.setName("Task 1");
        taskDTO.setDependencies(List.of());

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

        // Mock project plan with start and end dates
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setName("Test Project");
        projectPlan.setId(1L);
        projectPlan.setProjectStartDate(LocalDate.of(2024, 1, 1)); // Mock start date
        projectPlan.setProjectEndDate(null);   // Mock end date
        projectPlan.setProjectDuration(31); // Mock duration

        Task task = new Task();
        task.setId(1L);
        task.setName("Test Task");
        task.setDuration(5);
        task.setTaskStartDate(LocalDate.of(2024, 1, 1));  // Mock task start date
        task.setTaskEndDate(null);    // Mock task end date
        task.setDependencies(new ArrayList<>());  // No dependencies for simplicity

        // Set tasks in project plan
        projectPlan.setTasks(List.of(task));

        // Mock the repository call
        when(projectPlanRepository.findAll()).thenReturn(List.of(projectPlan));

        // Call the method under test
        List<ProjectPlanDetails> details = projectPlanService.toProjectPlanDetails();

        // Assertions
        assertNotNull(details);
        assertEquals(1, details.size());
        assertEquals("Test Project", details.get(0).getProjectPlanName());
        assertEquals(1, details.get(0).getTasks().size());
        assertEquals("Test Task", details.get(0).getTasks().get(0).getTaskName());

        // Assert formatted dates
        assertEquals("January 1, 2024", details.get(0).getProjectStart());
        assertEquals("N/A", details.get(0).getProjectEnd());
        assertEquals("January 1, 2024", details.get(0).getTasks().get(0).getStartDate());
        assertEquals("N/A", details.get(0).getTasks().get(0).getEndDate());

        // Assert task dependencies are empty (no dependencies were set)
        assertTrue(details.get(0).getTasks().get(0).getDependencies().isEmpty());
    }

    @Test
    void testUpdateTask() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("Updated Task");
        taskDTO.setDuration(5);
        taskDTO.setDependencies(new ArrayList<>());
        taskDTO.setProjectPlanId(1L);

        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setTasks(List.of());
        projectPlanRepository.save(projectPlan);

        Task task = new Task();
        task.setId(4L);
        task.setName("Old Task");
        task.setProjectPlan(projectPlan);

        when(taskRepository.findById(4L)).thenReturn(java.util.Optional.of(task));

        // Call the method under test
        projectPlanService.updateTask(4L, taskDTO);


        // Assertions
        assertEquals("Updated Task", task.getName());
        assertEquals(5, task.getDuration());
        verify(taskRepository, times(1)).findById(4L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void testUpdateProject() {
        // Prepare mock data
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setId(1L);
        projectPlan.setName("Old Project");
        projectPlan.setTasks(new ArrayList<>());  // Initialize the tasks list to avoid NullPointerException

        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setName("Updated Project");
        projectPlanDTO.setProjectStartDate(LocalDate.now());

        when(projectPlanRepository.findById(1L)).thenReturn(java.util.Optional.of(projectPlan));

        // Call the method under test
        projectPlanService.updateProject(1L, projectPlanDTO);

        // Assertions
        assertEquals("Updated Project", projectPlan.getName());
        assertEquals(LocalDate.now(), projectPlan.getProjectStartDate());
        verify(projectPlanRepository, times(1)).findById(1L);
        verify(projectPlanRepository, times(1)).save(any(ProjectPlan.class));
    }

    @Test
    void testRecalculateTaskAndProjectDates() {
        // Prepare mock data
        ProjectPlan projectPlan = new ProjectPlan();
        projectPlan.setProjectStartDate(LocalDate.of(2024, 10, 1));  // Initialize project start date
        projectPlan.setTasks(new ArrayList<>());  // Initialize the tasks list

        Task task1 = new Task("Task 1", 5, new ArrayList<>(), projectPlan);
        Task task2 = new Task("Task 2", 10, List.of(task1), projectPlan);

        projectPlan.getTasks().add(task1);  // Add tasks to the project
        projectPlan.getTasks().add(task2);

        // Call the method under test
        projectPlanService.recalculateTaskAndProjectDates(projectPlan);

        // Assertions: check if task and project dates are recalculated
        assertNotNull(task1.getTaskStartDate());
        assertNotNull(task1.getTaskEndDate());
        assertNotNull(task2.getTaskStartDate());
        assertNotNull(task2.getTaskEndDate());
        assertEquals(task2.getTaskEndDate(), projectPlan.getProjectEndDate());
    }
}
