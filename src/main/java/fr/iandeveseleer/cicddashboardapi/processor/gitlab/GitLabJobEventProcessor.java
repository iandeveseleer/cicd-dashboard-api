package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.model.Status;
import fr.iandeveseleer.cicddashboardapi.model.entity.Job;
import fr.iandeveseleer.cicddashboardapi.model.entity.Pipeline;
import fr.iandeveseleer.cicddashboardapi.processor.CIEventProcessor;
import fr.iandeveseleer.cicddashboardapi.repository.JobRepository;
import fr.iandeveseleer.cicddashboardapi.repository.PipelineRepository;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.webhook.BuildEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.ZoneOffset;

@Slf4j
@Service
public class GitLabJobEventProcessor implements CIEventProcessor {

    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;
    private final PipelineRepository pipelineRepository;

    public GitLabJobEventProcessor(@Qualifier("gitlabWebhookObjectMapper") ObjectMapper objectMapper, JobRepository jobRepository, PipelineRepository pipelineRepository) {
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
        this.pipelineRepository = pipelineRepository;
    }

    @Override
    public <T extends Serializable> void processEvent(T payload) {
        // Convert payload to BuildEvent
        BuildEvent buildEvent = objectMapper.convertValue(payload, BuildEvent.class);
        log.debug("Received GitLab job event for job ID {} with status {} (pipeline ID {}, project ID {})",
                buildEvent.getBuildId(),
                buildEvent.getBuildStatus(),
                buildEvent.getPipelineId(),
                buildEvent.getProjectId());

        // Find the pipeline associated with the job
        Pipeline pipeline = pipelineRepository.findByCiId((buildEvent.getPipelineId()));

        // If pipeline not found, log a warning and return
        if(pipeline == null) {
            log.debug("No pipeline found with ID {}", buildEvent.getPipelineId());
            return;
        }
        // Check if the job already exists
        Job job = jobRepository.findByCiId(buildEvent.getBuildId());
        if (job != null) {
            log.debug("Job with ID {} already exists for pipeline {}", job.getCiId(), pipeline.getCiId());
            // If status has changed, update it
            if (job.getStatus() != Status.fromString(buildEvent.getBuildStatus())) {
                job.setStatus(Status.fromString(buildEvent.getBuildStatus()));
                log.debug("Updated status of job ID {} to {}", job.getId(), job.getStatus());
            }
        } else {
            // Create a new job
            job = new Job();
            job.setCiId(buildEvent.getBuildId());
            job.setName(buildEvent.getBuildName());
            job.setStatus(Status.fromString(buildEvent.getBuildStatus()));
            job.setPipeline(pipeline);
            job.setLogsUrl(buildEvent.getProject().getWebUrl() + "/-/jobs/" + buildEvent.getBuildId());
            log.debug("Created new job with ID {} for pipeline {}", job.getId(), pipeline.getCiId());
        }
        if(buildEvent.getBuildStartedAt() != null) {
            job.setStartDate(buildEvent.getBuildStartedAt().toInstant().atOffset(ZoneOffset.UTC));
        }
        if(buildEvent.getBuildFinishedAt() != null) {
            job.setEndDate(buildEvent.getBuildFinishedAt().toInstant().atOffset(ZoneOffset.UTC));
        }
        jobRepository.save(job);
    }
}
