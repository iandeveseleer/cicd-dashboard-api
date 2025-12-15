    package fr.iandeveseleer.cicddashboardapi.processor.gitlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.iandeveseleer.cicddashboardapi.model.Status;
import fr.iandeveseleer.cicddashboardapi.model.entity.Job;
import fr.iandeveseleer.cicddashboardapi.model.entity.Pipeline;
import fr.iandeveseleer.cicddashboardapi.repository.JobRepository;
import fr.iandeveseleer.cicddashboardapi.repository.PipelineRepository;
import org.gitlab4j.api.webhook.BuildEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitLabJobEventProcessor - Unit Tests")
class GitLabJobEventProcessorTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private GitLabJobEventProcessor jobEventProcessor;

    private BuildEvent buildEvent;
    private Pipeline pipeline;
    private Job existingJob;

    @BeforeEach
    void setUp() {
        buildEvent = new BuildEvent();
        buildEvent.setBuildId(100L);
        buildEvent.setBuildName("test-job");
        buildEvent.setBuildStatus("success");
        buildEvent.setPipelineId(200L);
        buildEvent.setProjectId(300L);

        pipeline = new Pipeline();
        pipeline.setId(1L);
        pipeline.setCiId(200L);

        existingJob = new Job();
        existingJob.setId(10L);
        existingJob.setCiId(100L);
        existingJob.setName("test-job");
        existingJob.setStatus(Status.WAITING);
        existingJob.setPipeline(pipeline);
    }

    @Nested
    @DisplayName("Nominal Cases")
    class NominalCases {

        @Test
        @DisplayName("NC-01: Should create a new job successfully")
        void shouldCreateNewJobSuccessfully() {
            // Given
            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals(100L, savedJob.getCiId());
            assertEquals("test-job", savedJob.getName());
            assertEquals(Status.SUCCESS, savedJob.getStatus());
            assertEquals(pipeline, savedJob.getPipeline());
        }

        @Test
        @DisplayName("NC-02: Should update existing job with new status")
        void shouldUpdateExistingJobWithNewStatus() {
            // Given
            buildEvent.setBuildStatus("failed");
            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(existingJob);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals(Status.FAILED, savedJob.getStatus());
            assertEquals(existingJob.getId(), savedJob.getId());
        }

        @Test
        @DisplayName("NC-03: Should set job start and end dates")
        void shouldSetJobStartAndEndDates() {
            // Given
            Date startDate = new Date();
            Date endDate = new Date(startDate.getTime() + 60000);
            buildEvent.setBuildStartedAt(startDate);
            buildEvent.setBuildFinishedAt(endDate);

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertNotNull(savedJob.getStartDate());
            assertNotNull(savedJob.getEndDate());
        }

        @Test
        @DisplayName("NC-04: Should keep status if unchanged")
        void shouldKeepStatusIfUnchanged() {
            // Given
            buildEvent.setBuildStatus("pending");
            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(existingJob);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals(Status.WAITING, savedJob.getStatus());
        }
    }

    @Nested
    @DisplayName("Boundary Cases")
    class BoundaryCases {

        @Test
        @DisplayName("BC-01: Should handle job without dates")
        void shouldHandleJobWithoutDates() {
            // Given
            buildEvent.setBuildStartedAt(null);
            buildEvent.setBuildFinishedAt(null);

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertNull(savedJob.getStartDate());
            assertNull(savedJob.getEndDate());
        }

        @Test
        @DisplayName("BC-02: Should handle job with only start date")
        void shouldHandleJobWithOnlyStartDate() {
            // Given
            Date startDate = new Date();
            buildEvent.setBuildStartedAt(startDate);
            buildEvent.setBuildFinishedAt(null);

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertNotNull(savedJob.getStartDate());
            assertNull(savedJob.getEndDate());
        }

        @Test
        @DisplayName("BC-03: Should handle job with empty name")
        void shouldHandleJobWithEmptyName() {
            // Given
            buildEvent.setBuildName("");

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals("", savedJob.getName());
        }
    }

    @Nested
    @DisplayName("Error Cases")
    class ErrorCases {

        @Test
        @DisplayName("EC-01: Should not create job when pipeline not found")
        void shouldNotCreateJobWhenPipelineNotFound() {
            // Given
            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            verify(jobRepository, never()).save(any());
        }

        @Test
        @DisplayName("EC-02: Should handle null status")
        void shouldHandleNullStatus() {
            // Given
            buildEvent.setBuildStatus(null);

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals(Status.UNKNOWN, savedJob.getStatus());
        }

        @Test
        @DisplayName("EC-03: Should handle invalid status")
        void shouldHandleInvalidStatus() {
            // Given
            buildEvent.setBuildStatus("invalid_status");

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertNotNull(savedJob);
        }
    }

    @Nested
    @DisplayName("Special Cases")
    class SpecialCases {

        @Test
        @DisplayName("SC-01: Should handle multiple status updates for the same job")
        void shouldHandleMultipleStatusUpdates() {
            // Given
            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(existingJob);

            buildEvent.setBuildStatus("running");
            jobEventProcessor.processEvent(buildEvent);

            buildEvent.setBuildStatus("success");
            jobEventProcessor.processEvent(buildEvent);

            // Then
            verify(jobRepository, times(2)).save(any(Job.class));
        }

        @Test
        @DisplayName("SC-02: Should handle job with zero CI ID")
        void shouldHandleJobWithZeroCiId() {
            // Given
            buildEvent.setBuildId(0L);

            when(objectMapper.convertValue(any(), eq(BuildEvent.class))).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(0L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(buildEvent);

            // Then
            ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
            verify(jobRepository).save(jobCaptor.capture());

            Job savedJob = jobCaptor.getValue();
            assertEquals(0L, savedJob.getCiId());
        }

        @Test
        @DisplayName("SC-03: Should convert payload to BuildEvent correctly")
        void shouldConvertPayloadToBuildEventCorrectly() {
            // Given
            String stringPayload = "test-payload";
            when(objectMapper.convertValue(stringPayload, BuildEvent.class)).thenReturn(buildEvent);
            when(pipelineRepository.findByCiId(200L)).thenReturn(pipeline);
            when(jobRepository.findByCiId(100L)).thenReturn(null);

            // When
            jobEventProcessor.processEvent(stringPayload);

            // Then
            verify(objectMapper).convertValue(stringPayload, BuildEvent.class);
            verify(jobRepository).save(any(Job.class));
        }
    }
}

