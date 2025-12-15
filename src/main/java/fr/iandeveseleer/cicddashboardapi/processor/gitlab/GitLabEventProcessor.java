package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.exception.EventParsingException;
import fr.iandeveseleer.cicddashboardapi.processor.CIEventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.webhook.BuildEvent;
import org.gitlab4j.api.webhook.PipelineEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Slf4j
@Service
public class GitLabEventProcessor implements CIEventProcessor {

    private final GitLabJobEventProcessor jobEventProcessor;
    private final GitLabPipelineEventProcessor pipelineEventProcessor;
    private final ObjectMapper objectMapper;

    public GitLabEventProcessor(GitLabJobEventProcessor jobEventProcessor,
                                GitLabPipelineEventProcessor pipelineEventProcessor,
                                @Qualifier("gitlabWebhookObjectMapper") ObjectMapper objectMapper) {
        this.jobEventProcessor = jobEventProcessor;
        this.pipelineEventProcessor = pipelineEventProcessor;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T extends Serializable> void processEvent(T payload) {
        try {
            if (payload instanceof String jsonPayload) {
                JsonNode root = objectMapper.readTree(jsonPayload);
                JsonNode objectKindNode = root.get("object_kind");
                if (objectKindNode == null || objectKindNode.isNull()) {
                    throw new EventParsingException("Missing 'object_kind' in GitLab event payload");
                }
                String objectKind = objectKindNode.asText();
                switch (objectKind) {
                    case "pipeline":
                        PipelineEvent pipelineEvent = objectMapper.treeToValue(root, PipelineEvent.class);
                        pipelineEventProcessor.processEvent(pipelineEvent);
                        break;
                    case "build":
                        BuildEvent buildEvent = objectMapper.treeToValue(root, BuildEvent.class);
                        jobEventProcessor.processEvent(buildEvent);
                        break;
                    default:
                        throw new EventParsingException("Event type not yet supported: " + objectKind);
                }
                return;
            }

            throw new EventParsingException("Unsupported payload type: " + payload.getClass().getName());
        } catch (EventParsingException e) {
            throw e;
        } catch (Exception e) {
            throw new EventParsingException("Error while processing GitLab event: " + e.getMessage(), e);
        }
    }
}
