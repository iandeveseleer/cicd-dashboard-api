package fr.iandeveseleer.cicddashboardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PipelineDTO {
    private Long id;
    private String sha1;
    private String status;
    private String url;
    private OffsetDateTime createdDate;
    private List<JobDTO> jobs;
}
