package fr.iandeveseleer.cicddashboardapi.service.gitlab;

import fr.iandeveseleer.cicddashboardapi.dto.BranchDTO;
import fr.iandeveseleer.cicddashboardapi.dto.GitLabProjectDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Branch;
import org.gitlab4j.api.models.Project;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitLabService {

    private final GitLabApi gitLabApi;

    /**
     * Retrieve a GitLab project by its path (path with namespace)
     * Example: "SG1/ALPHA"
     * @param repositoryPath the project path (format: group/project)
     * @return the GitLab project
     * @throws GitLabApiException if the project does not exist or authentication error occurs
     */
    public GitLabProjectDTO getProject(String repositoryPath) throws GitLabApiException {
        log.debug("Fetching GitLab project with path: {}", repositoryPath);
        Project project = gitLabApi.getProjectApi().getProject(repositoryPath);
        return mapProjectToDTO(project);
    }

    /**
     * Retrieve all branches of a GitLab project
     *
     * @param repositoryId the project ID (integer)
     * @return list of branches in the project
     * @throws GitLabApiException if the project does not exist or authentication error occurs
     */
    public List<BranchDTO> getProjectBranches(int repositoryId) throws GitLabApiException {
        log.debug("Fetching branches for GitLab project: {}", repositoryId);
        Project project = gitLabApi.getProjectApi().getProject(repositoryId);
        List<Branch> branches = gitLabApi.getRepositoryApi().getBranches(project.getId());

        return branches.stream()
                .map(this::mapBranchToDTO)
                .collect(Collectors.toList());
    }
    /**
     * Search for GitLab projects that start with a given search string
     *
     * @param searchPattern the pattern to search for (project name starting with this string)
     * @return list of GitLab projects matching the pattern
     * @throws GitLabApiException if an authentication error occurs
     */
    public List<GitLabProjectDTO> searchProjectsByName(String searchPattern) throws GitLabApiException {
        log.debug("Searching for GitLab projects starting with: {}", searchPattern);
        List<Project> projects = gitLabApi.getProjectApi().getProjects(searchPattern);

        return projects.stream()
                .filter(project -> project.getName().startsWith(searchPattern) ||
                        project.getPathWithNamespace().startsWith(searchPattern))
                .map(this::mapProjectToDTO)
                .collect(Collectors.toList());
    }

    private GitLabProjectDTO mapProjectToDTO(Project project) {
        return new GitLabProjectDTO(
                project.getId(),
                project.getName(),
                project.getPathWithNamespace(),
                project.getWebUrl(),
                project.getVisibility() != null ? project.getVisibility().toString() : null,
                project.getDefaultBranch(),
                project.getDescription()
        );
    }

    private BranchDTO mapBranchToDTO(Branch branch) {
        return new BranchDTO(
                branch.getName(),
                branch.getCommit() != null ? branch.getCommit().getId() : null,
                branch.getDefault()
        );
    }
}

