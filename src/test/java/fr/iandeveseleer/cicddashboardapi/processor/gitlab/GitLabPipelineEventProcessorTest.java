package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.model.Status;
import fr.iandeveseleer.cicddashboardapi.model.entity.Pipeline;
import fr.iandeveseleer.cicddashboardapi.model.entity.Project;
import fr.iandeveseleer.cicddashboardapi.model.entity.ProjectVersion;
import fr.iandeveseleer.cicddashboardapi.repository.PipelineRepository;
import fr.iandeveseleer.cicddashboardapi.repository.ProjectRepository;
import org.gitlab4j.api.webhook.EventProject;
import org.gitlab4j.api.webhook.PipelineEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitLabPipelineEventProcessor - Unit Tests")
class GitLabPipelineEventProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private GitLabPipelineEventProcessor pipelineEventProcessor;

    private PipelineEvent pipelineEvent;
    private PipelineEvent.ObjectAttributes objectAttributes;
    private Project project;
    private ProjectVersion projectVersion;
    private Pipeline existingPipeline;

    @BeforeEach
    void setUp() {
        // Setup PipelineEvent
        pipelineEvent = new PipelineEvent();

        objectAttributes = new PipelineEvent.ObjectAttributes();
        objectAttributes.setId(100L);
        objectAttributes.setStatus("success");
        objectAttributes.setRef("main");
        objectAttributes.setSha("abc123");
        objectAttributes.setBeforeSha("def456");
        objectAttributes.setCreatedAt(new Date());

        EventProject eventProject = new EventProject();
        eventProject.setId(200L);
        eventProject.setWebUrl("https://gitlab.com/test/project");

        pipelineEvent.setObjectAttributes(objectAttributes);
        pipelineEvent.setProject(eventProject);

        // Setup Project
        project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setRepositoryId(200L);

        // Setup ProjectVersion
        projectVersion = new ProjectVersion();
        projectVersion.setId(10L);
        projectVersion.setVersion(1);
        projectVersion.setBranchId("main");
        projectVersion.setProject(project);

        List<ProjectVersion> versions = new ArrayList<>();
        versions.add(projectVersion);
        project.setVersions(versions);

        // Setup existing Pipeline
        existingPipeline = new Pipeline();
        existingPipeline.setId(5L);
        existingPipeline.setCiId(100L);
        existingPipeline.setStatus(Status.IN_PROGRESS);
        existingPipeline.setProjectVersion(projectVersion);
    }

    @Nested
    @DisplayName("Nominal Cases")
    class NominalCases {

        @Test
        @DisplayName("NC-01: Should create a new pipeline successfully")
        void shouldCreateNewPipelineSuccessfully() {
            // Given
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(100L, savedPipeline.getCiId());
            assertEquals("abc123", savedPipeline.getSha1());
            assertEquals("def456", savedPipeline.getPreviousSha1());
            assertEquals(Status.SUCCESS, savedPipeline.getStatus());
            assertNotNull(savedPipeline.getChangesUrl());
            assertNotNull(savedPipeline.getUrl());
            assertNotNull(savedPipeline.getCreatedDate());
            assertEquals(projectVersion, savedPipeline.getProjectVersion());
        }

        @Test
        @DisplayName("NC-02: Should update existing pipeline with new status")
        void shouldUpdateExistingPipelineWithNewStatus() {
            // Given
            objectAttributes.setStatus("failed");
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project))
                .thenReturn(Collections.singletonList(existingPipeline));

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(Status.FAILED, savedPipeline.getStatus());
            assertEquals(existingPipeline.getId(), savedPipeline.getId());
        }

        @Test
        @DisplayName("NC-03: Should not update when status is unchanged")
        void shouldNotUpdateWhenStatusUnchanged() {
            // Given
            objectAttributes.setStatus("running");
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project))
                .thenReturn(Collections.singletonList(existingPipeline));

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            verify(pipelineRepository, never()).save(any());
        }

        @Test
        @DisplayName("NC-04: Should generate changes URL correctly")
        void shouldGenerateChangesUrlCorrectly() {
            // Given
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            String expectedChangesUrl = "https://gitlab.com/test/project/-/compare/def456...abc123";
            assertEquals(expectedChangesUrl, savedPipeline.getChangesUrl());
        }

        @Test
        @DisplayName("NC-05: Should generate pipeline URL correctly")
        void shouldGeneratePipelineUrlCorrectly() {
            // Given
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            String expectedUrl = "https://gitlab.com/test/project/-/pipelines/100";
            assertEquals(expectedUrl, savedPipeline.getUrl());
        }

        @Test
        @DisplayName("NC-06: Should update existing pipeline with end date")
        void shouldUpdateExistingPipelineWithEndDate() {
            // Given
            Date date = new Date();
            objectAttributes.setFinishedAt(date);
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project))
                    .thenReturn(Collections.singletonList(existingPipeline));

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(date.toInstant().atOffset(ZoneOffset.UTC), savedPipeline.getEndDate());
            assertEquals(existingPipeline.getId(), savedPipeline.getId());
        }
    }

    @Nested
    @DisplayName("Boundary Cases")
    class BoundaryCases {

        @Test
        @DisplayName("BC-01: Should not set changes URL when sha is null")
        void shouldNotSetChangesUrlWhenShaIsNull() {
            // Given
            objectAttributes.setSha(null);
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertNull(savedPipeline.getChangesUrl());
        }

        @Test
        @DisplayName("BC-02: Should not set changes URL when beforeSha is null")
        void shouldNotSetChangesUrlWhenBeforeShaIsNull() {
            // Given
            objectAttributes.setBeforeSha(null);
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertNull(savedPipeline.getChangesUrl());
        }

        @Test
        @DisplayName("BC-03: Should handle project with multiple versions")
        void shouldHandleProjectWithMultipleVersions() {
            // Given
            ProjectVersion anotherVersion = new ProjectVersion();
            anotherVersion.setId(11L);
            anotherVersion.setVersion(2);
            anotherVersion.setBranchId("develop");
            anotherVersion.setProject(project);

            project.getVersions().add(anotherVersion);

            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(projectVersion, savedPipeline.getProjectVersion());
            assertEquals("main", savedPipeline.getProjectVersion().getBranchId());
        }

        @Test
        @DisplayName("BC-04: Should handle project with multiple pipelines")
        void shouldHandleProjectWithMultiplePipelines() {
            // Given
            Pipeline anotherPipeline = new Pipeline();
            anotherPipeline.setId(6L);
            anotherPipeline.setCiId(101L);
            anotherPipeline.setProjectVersion(projectVersion);

            List<Pipeline> existingPipelines = Arrays.asList(existingPipeline, anotherPipeline);

            objectAttributes.setStatus("failed");
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(existingPipelines);

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(100L, savedPipeline.getCiId());
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("EC-01: Should do nothing if project not found")
        void shouldDoNothingWhenProjectNotFound() {
            // Given
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.empty());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            verify(pipelineRepository, never()).save(any());
            verify(pipelineRepository, never()).findByProjectVersionProject(any());
        }

        @Test
        @DisplayName("EC-02: Should do nothing when no matching version found")
        void shouldDoNothingWhenNoMatchingVersionFound() {
            // Given
            objectAttributes.setRef("feature-branch");
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            verify(pipelineRepository, never()).save(any());
        }

        @Test
        @DisplayName("EC-03: Should handle null status")
        void shouldHandleNullStatus() {
            // Given
            objectAttributes.setStatus(null);
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(Status.UNKNOWN, savedPipeline.getStatus());
        }

        @Test
        @DisplayName("EC-04: Should handle invalid status")
        void shouldHandleInvalidStatus() {
            // Given
            objectAttributes.setStatus("invalid_status");
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertNotNull(savedPipeline);
        }
    }

    @Nested
    @DisplayName("Special Cases")
    class SpecialCases {

        @Test
        @DisplayName("SC-01: Should not update pipeline on successive identical status updates")
        void shouldNotUpdateOnSuccessiveIdenticalStatusUpdates() {
            // Given
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project))
                .thenReturn(Collections.singletonList(existingPipeline));

            // When - First update
            objectAttributes.setStatus("running");
            pipelineEventProcessor.processEvent(pipelineEvent);

            // When - Second update, should not trigger save again
            objectAttributes.setStatus("success");
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            verify(pipelineRepository, times(1)).save(any(Pipeline.class));
        }

        @Test
        @DisplayName("SC-02: Should handle pipeline with zero CI ID")
        void shouldHandlePipelineWithZeroCiId() {
            // Given
            objectAttributes.setId(0L);
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(0L, savedPipeline.getCiId());
        }

        @Test
        @DisplayName("SC-03: Should convert payload to PipelineEvent correctly")
        void shouldConvertPayloadToPipelineEventCorrectly() {
            // Given
            String stringPayload = "test-payload";
            when(objectMapper.convertValue(stringPayload, PipelineEvent.class)).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(stringPayload);

            // Then
            verify(objectMapper).convertValue(stringPayload, PipelineEvent.class);
            verify(pipelineRepository).save(any(Pipeline.class));
        }

        @Test
        @DisplayName("SC-04: Should handle ref with special characters")
        void shouldHandleRefWithSpecialCharacters() {
            // Given
            String specialRef = "feature/JIRA-123/my-feature";
            projectVersion.setBranchId(specialRef);
            objectAttributes.setRef(specialRef);

            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            ArgumentCaptor<Pipeline> pipelineCaptor = ArgumentCaptor.forClass(Pipeline.class);
            verify(pipelineRepository).save(pipelineCaptor.capture());

            Pipeline savedPipeline = pipelineCaptor.getValue();
            assertEquals(specialRef, savedPipeline.getProjectVersion().getBranchId());
        }

        @Test
        @DisplayName("SC-05: Should handle project without versions")
        void shouldHandleProjectWithoutVersions() {
            // Given
            project.setVersions(new ArrayList<>());
            when(objectMapper.convertValue(any(), eq(PipelineEvent.class))).thenReturn(pipelineEvent);
            when(projectRepository.findByRepositoryId(200L)).thenReturn(Optional.of(project));
            when(pipelineRepository.findByProjectVersionProject(project)).thenReturn(new ArrayList<>());

            // When
            pipelineEventProcessor.processEvent(pipelineEvent);

            // Then
            verify(pipelineRepository, never()).save(any());
        }
    }
}

