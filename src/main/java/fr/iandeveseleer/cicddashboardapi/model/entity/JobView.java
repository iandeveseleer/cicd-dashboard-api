package fr.iandeveseleer.cicddashboardapi.model.entity;

import fr.iandeveseleer.cicddashboardapi.model.Status;
import org.springframework.data.rest.core.config.Projection;

import java.time.OffsetDateTime;

@Projection(name = "full", types = Job.class)
public interface JobView {

    Long getId();
    String getName();
    Status getStatus();
    String getLogsUrl();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();
    JobDetailsView getDetails();
}