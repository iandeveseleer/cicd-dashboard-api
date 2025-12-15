package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.exception.EventParsingException;
import org.gitlab4j.api.webhook.BuildEvent;
import org.gitlab4j.api.webhook.PipelineEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitLabEventProcessor - Unit Tests")
class GitLabEventProcessorTest {

    @Mock
    private GitLabJobEventProcessor jobEventProcessor;

    @Mock
    private GitLabPipelineEventProcessor pipelineEventProcessor;

    @Mock
    private ObjectMapper objectMapper;

    private GitLabEventProcessor gitLabEventProcessor;
    private ObjectMapper realObjectMapper;

    @BeforeEach
    void setUp() {
        realObjectMapper = new ObjectMapper();
        gitLabEventProcessor = new GitLabEventProcessor(jobEventProcessor, pipelineEventProcessor, objectMapper);
    }

    @Nested
    @DisplayName("Nominal Cases")
    class NominalCases {

        @Test
        @DisplayName("NC-01: Should process a valid pipeline event")
        void shouldProcessValidPipelineEvent() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"pipeline\",\"object_attributes\":{\"id\":123}}";
            PipelineEvent mockPipelineEvent = mock(PipelineEvent.class);
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);
            when(objectMapper.treeToValue(jsonNode, PipelineEvent.class)).thenReturn(mockPipelineEvent);

            // When
            gitLabEventProcessor.processEvent(jsonPayload);

            // Then
            verify(pipelineEventProcessor, times(1)).processEvent(mockPipelineEvent);
            verify(jobEventProcessor, never()).processEvent(any());
        }

        @Test
        @DisplayName("NC-02: Should process a valid build event")
        void shouldProcessValidBuildEvent() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"build\",\"build_id\":456}";
            BuildEvent mockBuildEvent = mock(BuildEvent.class);
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);
            when(objectMapper.treeToValue(jsonNode, BuildEvent.class)).thenReturn(mockBuildEvent);

            // When
            gitLabEventProcessor.processEvent(jsonPayload);

            // Then
            verify(jobEventProcessor, times(1)).processEvent(mockBuildEvent);
            verify(pipelineEventProcessor, never()).processEvent(any());
        }
    }

    @Nested
    @DisplayName("Boundary Cases")
    class BoundaryCases {

        @Test
        @DisplayName("BC-01: Should reject payload with null object_kind")
        void shouldRejectPayloadWithNullObjectKind() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":null}";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertEquals("Missing 'object_kind' in GitLab event payload", exception.getMessage());
            verify(pipelineEventProcessor, never()).processEvent(any());
            verify(jobEventProcessor, never()).processEvent(any());
        }

        @Test
        @DisplayName("BC-02: Should reject payload without object_kind")
        void shouldRejectPayloadWithoutObjectKind() throws Exception {
            // Given
            String jsonPayload = "{\"some_field\":\"value\"}";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertEquals("Missing 'object_kind' in GitLab event payload", exception.getMessage());
        }

        @Test
        @DisplayName("BC-03: Should reject empty payload")
        void shouldRejectEmptyPayload() throws Exception {
            // Given
            String jsonPayload = "{}";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertEquals("Missing 'object_kind' in GitLab event payload", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("EC-01: Should throw exception for unsupported event type")
        void shouldThrowExceptionForUnsupportedEventType() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"merge_request\"}";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertTrue(exception.getMessage().contains("Event type not yet supported: merge_request"));
            verify(pipelineEventProcessor, never()).processEvent(any());
            verify(jobEventProcessor, never()).processEvent(any());
        }

        @Test
        @DisplayName("EC-02: Should throw exception for unsupported payload type")
        void shouldThrowExceptionForUnsupportedPayloadType() {
            // Given
            Integer invalidPayload = 123;

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(invalidPayload)
            );

            assertTrue(exception.getMessage().contains("Unsupported payload type"));
            verify(pipelineEventProcessor, never()).processEvent(any());
            verify(jobEventProcessor, never()).processEvent(any());
        }

        @Test
        @DisplayName("EC-03: Should throw exception for invalid JSON")
        void shouldThrowExceptionForInvalidJson() throws Exception {
            // Given
            String invalidJson = "{invalid json";

            doThrow(new RuntimeException("Invalid JSON")).when(objectMapper).readTree(invalidJson);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(invalidJson)
            );

            assertTrue(exception.getMessage().contains("Error while processing GitLab event"));
        }

        @Test
        @DisplayName("EC-04: Should propagate original event parsing exception")
        void shouldPropagateOriginalEventParsingException() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"pipeline\"}";
            String originalMessage = "Original parsing error";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);
            when(objectMapper.treeToValue(jsonNode, PipelineEvent.class))
                .thenThrow(new EventParsingException(originalMessage));

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertEquals(originalMessage, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Special Cases")
    class SpecialCases {

        @Test
        @DisplayName("SC-01: Should handle payload with extra fields")
        void shouldHandlePayloadWithExtraFields() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"pipeline\",\"extra_field\":\"value\",\"another\":123}";
            PipelineEvent mockPipelineEvent = mock(PipelineEvent.class);
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);
            when(objectMapper.treeToValue(jsonNode, PipelineEvent.class)).thenReturn(mockPipelineEvent);

            // When
            gitLabEventProcessor.processEvent(jsonPayload);

            // Then
            verify(pipelineEventProcessor, times(1)).processEvent(mockPipelineEvent);
        }

        @Test
        @DisplayName("SC-02: Should handle object_kind with leading/trailing spaces")
        void shouldHandleObjectKindWithSpaces() throws Exception {
            // Given
            String jsonPayload = "{\"object_kind\":\"  pipeline  \"}";
            JsonNode jsonNode = realObjectMapper.readTree(jsonPayload);

            when(objectMapper.readTree(jsonPayload)).thenReturn(jsonNode);

            // When & Then
            EventParsingException exception = assertThrows(
                EventParsingException.class,
                () -> gitLabEventProcessor.processEvent(jsonPayload)
            );

            assertTrue(exception.getMessage().contains("Event type not yet supported"));
        }
    }
}

