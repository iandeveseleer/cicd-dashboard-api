package fr.iandeveseleer.cicddashboardapi.model.entity;

import fr.iandeveseleer.cicddashboardapi.model.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
public class Job {

    @Id
    @GeneratedValue
    private Long id;
    private Long ciId;
    private String name;
    @ManyToOne
    @JoinColumn(name = "pipeline_id")
    private Pipeline pipeline;
    @Enumerated(EnumType.STRING)
    @Basic
    private Status status;
    @ManyToOne
    @JoinColumn(name = "details_id")
    private JobDetails details;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private String logsUrl;
}
