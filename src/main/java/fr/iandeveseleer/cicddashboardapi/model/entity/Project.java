package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String repositoryUrl;
    private Long repositoryId;
    @OneToMany(mappedBy = "project")
    @OrderBy("version DESC")
    private List<ProjectVersion> versions;
}
