package fr.iandeveseleer.cicddashboardapi.controller;

import fr.iandeveseleer.cicddashboardapi.exception.EventParsingException;
import fr.iandeveseleer.cicddashboardapi.processor.gitlab.GitLabEventProcessor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "CI Webhooks Controller")
@RequestMapping("/webhooks")
@RequiredArgsConstructor
public class GitLabWebhookController {

    private final GitLabEventProcessor eventProcessorService;

    @PostMapping("/gitlab")
    @Operation(summary = "Endpoint to receive GitLab webhook events", description = "Processes incoming GitLab webhook events such as pipeline and job updates.")
    public ResponseEntity<String> handleGitLabEvent(
            @RequestBody String payload) {

        try {
            eventProcessorService.processEvent(payload);
            return ResponseEntity.noContent().build();
        } catch (EventParsingException e) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("Invalid GitLab event format: " + e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while processing GitLab event: " + e.getMessage());
        }
    }
}
