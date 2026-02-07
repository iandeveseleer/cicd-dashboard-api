package fr.iandeveseleer.cicddashboardapi.controller;

import fr.iandeveseleer.cicddashboardapi.dto.BranchDTO;
import fr.iandeveseleer.cicddashboardapi.dto.GitLabProjectDTO;
import fr.iandeveseleer.cicddashboardapi.service.gitlab.GitLabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "GitLab Projects Controller")
@RequestMapping("/gitlab/projects")
@RequiredArgsConstructor
public class GitLabProjectController {

    private final GitLabService gitLabService;

    @GetMapping
    @Operation(
            summary = "Retrieve GitLab project information by path",
            description = "Retrieve detailed information about a GitLab project using its path (format: group/project, e.g., SG1/ALPHA)"
    )
    public ResponseEntity<GitLabProjectDTO> getProject(
            @RequestParam(name = "path")
            @Parameter(description = "Project path in the format 'group/project', e.g., 'SG1/ALPHA'")
            String path) {

        try {
            GitLabProjectDTO project = gitLabService.getProject(path);
            return ResponseEntity.ok(project);
        } catch (GitLabApiException e) {
            log.error("Error fetching GitLab project: {}", path, e);
            if (e.getHttpStatus() == 404) {
                return ResponseEntity.notFound().build();
            }
            if (e.getHttpStatus() == 401 || e.getHttpStatus() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching GitLab project: {}", path, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/branches")
    @Operation(
            summary = "Retrieve list of branches for a GitLab project",
            description = "Retrieve a list of branches for a GitLab project using its ID"
    )
    public ResponseEntity<List<BranchDTO>> getProjectBranches(
            @RequestParam(name = "id")
            @Parameter(description = "Project ID")
            int id) {

        try {
            List<BranchDTO> branches = gitLabService.getProjectBranches(id);
            return ResponseEntity.ok(branches);
        } catch (GitLabApiException e) {
            log.error("Error fetching branches for GitLab project: {}", id, e);
            if (e.getHttpStatus() == 404) {
                return ResponseEntity.notFound().build();
            }
            if (e.getHttpStatus() == 401 || e.getHttpStatus() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error fetching branches for GitLab project: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search for GitLab projects by name pattern",
            description = "Search for GitLab projects where name or path starts with the given search pattern (case-sensitive)"
    )
    public ResponseEntity<List<GitLabProjectDTO>> searchProjects(
            @RequestParam(name = "pattern")
            @Parameter(description = "Search pattern to match project name or path (e.g., 'SG1', 'ALPHA')")
            String pattern) {

        try {
            List<GitLabProjectDTO> projects = gitLabService.searchProjectsByName(pattern);
            return ResponseEntity.ok(projects);
        } catch (GitLabApiException e) {
            log.error("Error searching GitLab projects with pattern: {}", pattern, e);
            if (e.getHttpStatus() == 401 || e.getHttpStatus() == 403) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Unexpected error searching GitLab projects with pattern: {}", pattern, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

