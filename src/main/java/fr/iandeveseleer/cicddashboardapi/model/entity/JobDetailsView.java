package fr.iandeveseleer.cicddashboardapi.model.entity;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = JobDetails.class)
public interface JobDetailsView {
    Long getId();
    String getLogUrl();
    JobDetailsLinksView getLinks();
}