package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "project_versions")
@Getter
@Setter
public class ProjectVersion {

    @Id
    @GeneratedValue
    private Long id;
    private Integer version;
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
    private String branchId;
    @OneToMany(mappedBy = "projectVersion")
    @OrderBy("createdDate DESC")
    private List<Pipeline> pipelines;
}
