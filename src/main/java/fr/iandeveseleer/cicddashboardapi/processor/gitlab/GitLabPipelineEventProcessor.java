package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.model.Status;
import fr.iandeveseleer.cicddashboardapi.model.entity.Pipeline;
import fr.iandeveseleer.cicddashboardapi.model.entity.Project;
import fr.iandeveseleer.cicddashboardapi.model.entity.ProjectVersion;
import fr.iandeveseleer.cicddashboardapi.processor.CIEventProcessor;
import fr.iandeveseleer.cicddashboardapi.repository.PipelineRepository;
import fr.iandeveseleer.cicddashboardapi.repository.ProjectRepository;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.webhook.PipelineEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class GitLabPipelineEventProcessor implements CIEventProcessor {

    private final ObjectMapper objectMapper;
    private final ProjectRepository projectRepository;
    private final PipelineRepository pipelineRepository;

    public GitLabPipelineEventProcessor(@Qualifier("gitlabWebhookObjectMapper") ObjectMapper objectMapper, ProjectRepository projectRepository, PipelineRepository pipelineRepository) {
        this.objectMapper = objectMapper;
        this.projectRepository = projectRepository;
        this.pipelineRepository = pipelineRepository;
    }

    @Override
    public <T extends Serializable> void processEvent(T payload) {
        // Convert payload to PipelineEvent
        PipelineEvent pipelineEvent = objectMapper.convertValue(payload, PipelineEvent.class);
        log.debug("Received GitLab pipeline event for pipeline ID {} with status {} (project ID {})",
                pipelineEvent.getObjectAttributes().getId(),
                pipelineEvent.getObjectAttributes().getStatus(),
                pipelineEvent.getProject().getId());

        // Find the project by repository ID
        Optional<Project> optProject = projectRepository.findByRepositoryId(pipelineEvent.getProject().getId());

        // If project not found, log a warning and return
        if(optProject.isEmpty()) {
            log.debug("No project found with repository ID {}", pipelineEvent.getProject().getId());
            return;
        }

        // Retrieve existing pipelines for the project
        List<Pipeline> pipelines = pipelineRepository.findByProjectVersionProject(optProject.get());

        // Check if the pipeline already exists
        Optional<Pipeline> existingMatchingPipeline = pipelines.stream()
                .filter(p -> p.getCiId().equals(pipelineEvent.getObjectAttributes().getId()))
                .findFirst();

        // If it exists, and status is different, change the status to the new one, otherwise, create a new pipeline
        if (existingMatchingPipeline.isPresent()) {
            Pipeline pipeline = existingMatchingPipeline.get();
            log.debug("Pipeline with ID {} already exists for project {}", pipeline.getCiId(), pipeline.getProjectVersion().getProject().getName());
            if (!pipeline.getStatus().equals(Status.fromString(pipelineEvent.getObjectAttributes().getStatus()))) {
                pipeline.setStatus(Status.fromString(pipelineEvent.getObjectAttributes().getStatus()));
                log.debug("Updated status of pipeline ID {} to {}", pipeline.getCiId(), pipeline.getStatus());
            }
            if(pipeline.getEndDate() == null && pipelineEvent.getObjectAttributes().getFinishedAt() != null) {
                pipeline.setEndDate(pipelineEvent.getObjectAttributes().getFinishedAt().toInstant().atOffset(ZoneOffset.UTC));
                log.debug("Updated end date of pipeline ID {} to {}", pipeline.getCiId(), pipeline.getEndDate());
            }
            pipelineRepository.save(pipeline);
        } else {
            // Find matching project version by branch ref
            Project project = optProject.get();
            Optional<ProjectVersion> optProjectVersion = project.getVersions().stream()
                    .filter(v -> v.getBranchId().equals(pipelineEvent.getObjectAttributes().getRef()))
                    .findFirst();

            // Create and save new pipeline if matching project version is found
            if (optProjectVersion.isPresent()) {
                Pipeline newPipeline = new Pipeline();
                newPipeline.setCiId(pipelineEvent.getObjectAttributes().getId());
                newPipeline.setSha1(pipelineEvent.getObjectAttributes().getSha());
                newPipeline.setPreviousSha1(pipelineEvent.getObjectAttributes().getBeforeSha());
                newPipeline.setStatus(Status.fromString(pipelineEvent.getObjectAttributes().getStatus()));
                if(pipelineEvent.getObjectAttributes().getSha() != null && pipelineEvent.getObjectAttributes().getBeforeSha() != null) {
                    newPipeline.setChangesUrl(pipelineEvent.getProject().getWebUrl() + "/-/compare/" + pipelineEvent.getObjectAttributes().getBeforeSha() + "..." + pipelineEvent.getObjectAttributes().getSha());
                }
                newPipeline.setUrl(pipelineEvent.getProject().getWebUrl() + "/-/pipelines/" + pipelineEvent.getObjectAttributes().getId());
                newPipeline.setCreatedDate(pipelineEvent.getObjectAttributes().getCreatedAt().toInstant().atOffset(ZoneOffset.UTC));
                newPipeline.setProjectVersion(optProjectVersion.get());
                pipelineRepository.save(newPipeline);
                log.debug("Created new pipeline with ID {} for project version {}", newPipeline.getCiId(), optProjectVersion.get().getVersion());
            } else {
                log.debug("No matching project version found for ref {}", pipelineEvent.getObjectAttributes().getRef());
            }
        }

    }
}
