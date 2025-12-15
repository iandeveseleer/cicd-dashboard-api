package fr.iandeveseleer.cicddashboardapi.model.entity;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = JobDetailsLinks.class)
public interface JobDetailsLinksView {
    Long getId();
    String getName();
    String getUrl();
}
