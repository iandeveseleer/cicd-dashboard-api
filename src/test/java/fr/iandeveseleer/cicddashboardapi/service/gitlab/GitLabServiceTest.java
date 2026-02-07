package fr.iandeveseleer.cicddashboardapi.service.gitlab;

import fr.iandeveseleer.cicddashboardapi.dto.BranchDTO;
import fr.iandeveseleer.cicddashboardapi.dto.GitLabProjectDTO;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.ProjectApi;
import org.gitlab4j.api.RepositoryApi;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitLabServiceTest {

    @Mock
    private GitLabApi gitLabApi;

    @Mock
    private ProjectApi projectApi;

    @Mock
    private RepositoryApi repositoryApi;

    private GitLabService gitLabService;

    @BeforeEach
    void setUp() {
        gitLabService = new GitLabService(gitLabApi);
        when(gitLabApi.getProjectApi()).thenReturn(projectApi);
        lenient().when(gitLabApi.getRepositoryApi()).thenReturn(repositoryApi);
    }

    @Test
    void testGetProject_Success() throws Exception {
        // Arrange
        String repositoryPath = "SG1/ALPHA";
        Project mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setName("ALPHA");
        mockProject.setPathWithNamespace("SG1/ALPHA");
        mockProject.setWebUrl("http://gitlab.example.com/SG1/ALPHA");
        mockProject.setDefaultBranch("main");
        mockProject.setDescription("Test Project");

        when(projectApi.getProject(repositoryPath)).thenReturn(mockProject);

        // Act
        GitLabProjectDTO result = gitLabService.getProject(repositoryPath);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ALPHA");
        assertThat(result.getPathWithNamespace()).isEqualTo("SG1/ALPHA");
        assertThat(result.getDefaultBranch()).isEqualTo("main");

        verify(projectApi).getProject(repositoryPath);
    }

    @Test
    void testGetProjectBranches_Success() throws Exception {
        // Arrange
        String repositoryPath = "SG1/ALPHA";

        Project mockProject = new Project();
        mockProject.setId(1L);
        mockProject.setName("ALPHA");

        Branch mainBranch = new Branch();
        mainBranch.setName("main");
        mainBranch.setDefault(true);
        Commit mainCommit = new Commit();
        mainCommit.setId("abc123def456");
        mainBranch.setCommit(mainCommit);

        Branch developBranch = new Branch();
        developBranch.setName("develop");
        developBranch.setDefault(false);
        Commit devCommit = new Commit();
        devCommit.setId("def456abc123");
        developBranch.setCommit(devCommit);

        when(projectApi.getProject(1)).thenReturn(mockProject);
        when(repositoryApi.getBranches(1L)).thenReturn(Arrays.asList(mainBranch, developBranch));

        // Act
        List<BranchDTO> result = gitLabService.getProjectBranches(1);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("main");
        assertThat(result.get(0).getIsDefault()).isTrue();
        assertThat(result.get(1).getName()).isEqualTo("develop");
        assertThat(result.get(1).getIsDefault()).isFalse();

        verify(projectApi).getProject(1);
        verify(repositoryApi).getBranches(1L);
    }
}

