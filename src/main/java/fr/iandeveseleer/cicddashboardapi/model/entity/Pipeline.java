package fr.iandeveseleer.cicddashboardapi.model.entity;

import fr.iandeveseleer.cicddashboardapi.model.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "pipelines")
@Getter
@Setter
public class Pipeline {

    @Id
    @GeneratedValue
    private Long id;
    private Long ciId;
    private String sha1;
    private String previousSha1;
    private String changesUrl;
    @Enumerated(EnumType.STRING)
    @Basic
    private Status status;
    private String url;
    private OffsetDateTime createdDate;
    private OffsetDateTime endDate;
    @ManyToOne
    @JoinColumn(name = "project_version_id", nullable = false)
    @NotNull(message = "projectVersion is required to create a Pipeline.")
    private ProjectVersion projectVersion;
    @OneToMany(mappedBy = "pipeline")
    @OrderBy("endDate ASC")
    private List<Job> jobs;
}
