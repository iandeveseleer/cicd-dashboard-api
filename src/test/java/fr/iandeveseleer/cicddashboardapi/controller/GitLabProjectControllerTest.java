package fr.iandeveseleer.cicddashboardapi.controller;

import fr.iandeveseleer.cicddashboardapi.dto.BranchDTO;
import fr.iandeveseleer.cicddashboardapi.dto.GitLabProjectDTO;
import fr.iandeveseleer.cicddashboardapi.service.gitlab.GitLabService;
import org.gitlab4j.api.GitLabApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitLabProjectController.class)
class GitLabProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitLabService gitLabService;

    @Test
    void testGetProject_Success() throws Exception {
        // Arrange
        String repositoryPath = "SG1/ALPHA";
        GitLabProjectDTO mockProject = new GitLabProjectDTO(
                1L,
                "ALPHA",
                "SG1/ALPHA",
                "http://gitlab.example.com/SG1/ALPHA",
                "PRIVATE",
                "main",
                "Test Project"
        );

        when(gitLabService.getProject("SG1/ALPHA")).thenReturn(mockProject);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects")
                .param("path", "SG1/ALPHA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("ALPHA")))
                .andExpect(jsonPath("$.path_with_namespace", is("SG1/ALPHA")))
                .andExpect(jsonPath("$.default_branch", is("main")));

        verify(gitLabService).getProject("SG1/ALPHA");
    }

    @Test
    void testGetProject_NotFound() throws Exception {
        // Arrange
        String repositoryPath = "SG1/NONEXISTENT";
        GitLabApiException exception = new GitLabApiException("Not Found",404);

        when(gitLabService.getProject("SG1/NONEXISTENT")).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects")
                .param("path", "SG1/NONEXISTENT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(gitLabService).getProject("SG1/NONEXISTENT");
    }

    @Test
    void testGetProject_Forbidden() throws Exception {
        // Arrange
        String repositoryPath = "SG1/ALPHA";
        GitLabApiException exception = new GitLabApiException("Forbidden",403);

        when(gitLabService.getProject("SG1/ALPHA")).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects")
                .param("path", "SG1/ALPHA")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(gitLabService).getProject("SG1/ALPHA");
    }

    @Test
    void testGetProjectBranches_Success() throws Exception {
        // Arrange
        int repositoryId = 1;
        List<BranchDTO> mockBranches = Arrays.asList(
                new BranchDTO("main", "abc123def456", true),
                new BranchDTO("develop", "def456abc123", false)
        );

        when(gitLabService.getProjectBranches(repositoryId)).thenReturn(mockBranches);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/branches")
                .param("id", String.valueOf(repositoryId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("main")))
                .andExpect(jsonPath("$[0].is_default", is(true)))
                .andExpect(jsonPath("$[1].name", is("develop")))
                .andExpect(jsonPath("$[1].is_default", is(false)));

        verify(gitLabService).getProjectBranches(repositoryId);
    }

    @Test
    void testGetProjectBranches_NotFound() throws Exception {
        // Arrange
        int repositoryId = 6767;
        GitLabApiException exception = new GitLabApiException("Not Found", 404);

        when(gitLabService.getProjectBranches(repositoryId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/branches")
                .param("id", String.valueOf(repositoryId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(gitLabService).getProjectBranches(repositoryId);
    }

    @Test
    void testGetProjectBranches_Forbidden() throws Exception {
        // Arrange
        int repositoryId = 1;

        GitLabApiException exception = new GitLabApiException("Forbidden", 403);

        when(gitLabService.getProjectBranches(repositoryId)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/branches")
                .param("id", String.valueOf(repositoryId))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(gitLabService).getProjectBranches(repositoryId);
    }
}

