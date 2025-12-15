package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "job_details_links")
@Getter
@Setter
public class JobDetailsLinks {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private Integer url;
}
