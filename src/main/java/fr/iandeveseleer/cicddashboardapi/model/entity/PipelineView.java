package fr.iandeveseleer.cicddashboardapi.model.entity;

import org.springframework.data.rest.core.config.Projection;

import java.time.OffsetDateTime;
import java.util.List;

@Projection(name = "full", types = Pipeline.class)
public interface PipelineView {

    Long getId();
    String getSha1();
    String getStatus();
    String getUrl();
    OffsetDateTime getCreatedDate();
    OffsetDateTime getEndDate();
    List<JobView> getJobs();
}
