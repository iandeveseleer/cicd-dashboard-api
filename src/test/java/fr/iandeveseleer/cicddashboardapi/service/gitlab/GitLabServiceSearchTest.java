package fr.iandeveseleer.cicddashboardapi.service.gitlab;

import fr.iandeveseleer.cicddashboardapi.dto.GitLabProjectDTO;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLabServiceSearchTest {

    @Mock
    private GitLabApi gitLabApi;

    @Mock
    private ProjectApi projectApi;

    private GitLabService gitLabService;

    @BeforeEach
    void setUp() {
        gitLabService = new GitLabService(gitLabApi);
        when(gitLabApi.getProjectApi()).thenReturn(projectApi);
    }

    @Test
    void testSearchProjectsByName_Success() throws Exception {
        // Arrange
        String searchPattern = "SG1";

        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("ALPHA");
        project1.setPathWithNamespace("SG1/ALPHA");
        project1.setWebUrl("http://gitlab.example.com/SG1/ALPHA");
        project1.setDefaultBranch("main");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("BETA");
        project2.setPathWithNamespace("SG1/BETA");
        project2.setWebUrl("http://gitlab.example.com/SG1/BETA");
        project2.setDefaultBranch("main");

        when(projectApi.getProjects(searchPattern)).thenReturn(Arrays.asList(project1, project2));

        // Act
        List<GitLabProjectDTO> result = gitLabService.searchProjectsByName(searchPattern);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("ALPHA");
        assertThat(result.get(0).getPathWithNamespace()).isEqualTo("SG1/ALPHA");
        assertThat(result.get(1).getName()).isEqualTo("BETA");
        assertThat(result.get(1).getPathWithNamespace()).isEqualTo("SG1/BETA");

        verify(projectApi).getProjects(searchPattern);
    }

    @Test
    void testSearchProjectsByName_NoMatches() throws Exception {
        // Arrange
        String searchPattern = "NOTFOUND";
        when(projectApi.getProjects(searchPattern)).thenReturn(Arrays.asList());

        // Act
        List<GitLabProjectDTO> result = gitLabService.searchProjectsByName(searchPattern);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(projectApi).getProjects(searchPattern);
    }

    @Test
    void testSearchProjectsByName_FiltersByPattern() throws Exception {
        // Arrange
        String searchPattern = "ALPHA";

        Project project1 = new Project();
        project1.setId(1L);
        project1.setName("ALPHA");
        project1.setPathWithNamespace("SG1/ALPHA");
        project1.setWebUrl("http://gitlab.example.com/SG1/ALPHA");
        project1.setDefaultBranch("main");

        Project project2 = new Project();
        project2.setId(2L);
        project2.setName("BETA");
        project2.setPathWithNamespace("TEAM/PROJECT");
        project2.setWebUrl("http://gitlab.example.com/TEAM/PROJECT");
        project2.setDefaultBranch("main");

        when(projectApi.getProjects(searchPattern)).thenReturn(Arrays.asList(project1, project2));

        // Act
        List<GitLabProjectDTO> result = gitLabService.searchProjectsByName(searchPattern);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ALPHA");

        verify(projectApi).getProjects(searchPattern);
    }
}

