package fr.iandeveseleer.cicddashboardapi.controller;

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
class GitLabProjectControllerSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GitLabService gitLabService;

    @Test
    void testSearchProjects_Success() throws Exception {
        // Arrange
        String searchPattern = "SG1";
        List<GitLabProjectDTO> mockProjects = Arrays.asList(
                new GitLabProjectDTO(1L, "ALPHA", "SG1/ALPHA", "http://gitlab.example.com/SG1/ALPHA", "PRIVATE", "main", "Description 1"),
                new GitLabProjectDTO(2L, "BETA", "SG1/BETA", "http://gitlab.example.com/SG1/BETA", "PRIVATE", "main", "Description 2")
        );

        when(gitLabService.searchProjectsByName(searchPattern)).thenReturn(mockProjects);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/search")
                .param("pattern", searchPattern)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("ALPHA")))
                .andExpect(jsonPath("$[0].path_with_namespace", is("SG1/ALPHA")))
                .andExpect(jsonPath("$[1].name", is("BETA")))
                .andExpect(jsonPath("$[1].path_with_namespace", is("SG1/BETA")));

        verify(gitLabService).searchProjectsByName(searchPattern);
    }

    @Test
    void testSearchProjects_EmptyResult() throws Exception {
        // Arrange
        String searchPattern = "NOTFOUND";
        when(gitLabService.searchProjectsByName(searchPattern)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/search")
                .param("pattern", searchPattern)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(gitLabService).searchProjectsByName(searchPattern);
    }

    @Test
    void testSearchProjects_Forbidden() throws Exception {
        // Arrange
        String searchPattern = "SG1";
        GitLabApiException exception = new GitLabApiException("Forbidden", 403);

        when(gitLabService.searchProjectsByName(searchPattern)).thenThrow(exception);

        // Act & Assert
        mockMvc.perform(get("/api/gitlab/projects/search")
                .param("pattern", searchPattern)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(gitLabService).searchProjectsByName(searchPattern);
    }
}

