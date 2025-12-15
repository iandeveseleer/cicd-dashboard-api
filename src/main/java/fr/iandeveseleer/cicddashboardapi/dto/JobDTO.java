package fr.iandeveseleer.cicddashboardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private Long id;
    private String name;
    private String status;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
}
