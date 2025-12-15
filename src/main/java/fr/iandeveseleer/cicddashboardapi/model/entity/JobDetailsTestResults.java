package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_details_test_results")
@Getter
@Setter
public class JobDetailsTestResults {

    @Id
    @GeneratedValue
    private Long id;
    private Integer totalTests;
    private Integer passedTests;
    private Integer failedTests;
    private Integer skippedTests;
}
