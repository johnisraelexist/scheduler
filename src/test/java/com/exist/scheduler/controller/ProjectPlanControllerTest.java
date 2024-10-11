package com.exist.scheduler.controller;

import com.exist.scheduler.dto.ProjectPlanDTO;
import com.exist.scheduler.dto.TaskDTO;
import com.exist.scheduler.service.ProjectPlanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.NoSuchElementException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@WebMvcTest(ProjectPlanController.class)
class ProjectPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectPlanService projectPlanService;

    // Test for createProjectPlan endpoint
    @Test
    void createProjectPlan_Success() throws Exception {
        ProjectPlanDTO mockProjectPlan = new ProjectPlanDTO();
        mockProjectPlan.setId(1L);
        when(projectPlanService.createProjectPlan(any(ProjectPlanDTO.class))).thenReturn(mockProjectPlan);

        String requestBody = "{\"name\":\"New Project\"}";

        mockMvc.perform(post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Project plan created with ID: 1"))
                .andDo(print());
    }

    // Test for addTaskToProjectPlan endpoint when project exists
    @Test
    void addTaskToProjectPlan_Success() throws Exception {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setProjectPlanId(1L);

        doNothing().when(projectPlanService).addTaskToProjectPlan(any(TaskDTO.class));

        String requestBody = "{\"taskName\":\"Development\",\"duration\":10,\"projectPlanId\":1}";

        mockMvc.perform(post("/api/projects/add-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Task added to project plan with ID: 1"))
                .andDo(print());
    }

    // Test for addTaskToProjectPlan endpoint when project is not found
    @Test
    void addTaskToProjectPlan_ProjectNotFound() throws Exception {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setProjectPlanId(1L);

        Mockito.doThrow(new NoSuchElementException("Project Plan with ID 1 not found"))
                .when(projectPlanService).addTaskToProjectPlan(any(TaskDTO.class));

        String requestBody = "{\"taskName\":\"Development\",\"duration\":10,\"projectPlanId\":1}";

        mockMvc.perform(post("/api/projects/add-task")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Project Plan with ID 1 not found"))
                .andDo(print());
    }

    // Test for retrieveAllProjectPlan endpoint
    @Test
    void retrieveAllProjectPlan_Success() throws Exception {
        when(projectPlanService.toProjectPlanDetails()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/projects/retrieve-all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All Project Plans"))
                .andDo(print());
    }

    @Test
    void testUpdateTask() throws Exception {
        Long taskId = 1L;
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setName("Updated Task");
        taskDTO.setDuration(5);

        doNothing().when(projectPlanService).updateTask(taskId, taskDTO);

        mockMvc.perform(put("/api/projects/tasks/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Task\",\"duration\":5}")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Task updated and affected dates recalculated."));

        verify(projectPlanService).updateTask(taskId, taskDTO);
    }

    @Test
    void testUpdateProject() throws Exception {
        Long projectId = 1L;
        ProjectPlanDTO projectPlanDTO = new ProjectPlanDTO();
        projectPlanDTO.setName("Updated Project");

        doNothing().when(projectPlanService).updateProject(projectId, projectPlanDTO);

        mockMvc.perform(put("/api/projects/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Project\"}")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Project updated and affected dates recalculated."));

        verify(projectPlanService).updateProject(projectId, projectPlanDTO);
    }

    @Test
    void testDeleteTask() throws Exception {
        Long taskId = 1L;

        doNothing().when(projectPlanService).deleteTask(taskId);

        mockMvc.perform(delete("/api/projects/tasks/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted."));

        verify(projectPlanService).deleteTask(taskId);
    }

    @Test
    void testDeleteProject() throws Exception {
        Long projectId = 1L;

        doNothing().when(projectPlanService).deleteProject(projectId);

        mockMvc.perform(delete("/api/projects/{projectId}", projectId))
                .andExpect(status().isOk())
                .andExpect(content().string("Project deleted."));

        verify(projectPlanService).deleteProject(projectId);
    }
}
