package fr.iandeveseleer.cicddashboardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectVersionDTO {
    private Long id;
    private Integer version;
    private PipelineDTO lastPipeline;
}
