package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_details")
@Getter
@Setter
public class JobDetails {

    @Id
    @GeneratedValue
    private Long id;
    private String logUrl;
    @ManyToOne
    @JoinColumn(name = "links_id")
    private JobDetailsLinks links;
    @ManyToOne
    @JoinColumn(name = "test_results_id")
    private JobDetailsTestResults testResults;
    @ManyToOne
    @JoinColumn(name = "code_quality_id")
    private JobDetailsCodeQuality codeQuality;
}
