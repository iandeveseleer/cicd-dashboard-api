package fr.iandeveseleer.cicddashboardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GitLabProjectDTO {
    private Long id;
    private String name;
    private String pathWithNamespace;
    private String repositoryUrl;
    private String visibility;
    private String defaultBranch;
    private String description;
}

