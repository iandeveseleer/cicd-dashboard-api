package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_details_code_quality")
@Getter
@Setter
public class JobDetailsCodeQuality {

    @Id
    @GeneratedValue
    private Long id;
    private Integer coverage;
    private Integer duplications;
    private Integer criticalIssues;
    private Integer majorIssues;
    private Integer minorIssues;
}
