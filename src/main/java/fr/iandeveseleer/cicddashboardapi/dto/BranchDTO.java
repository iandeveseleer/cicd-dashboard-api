package fr.iandeveseleer.cicddashboardapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private String name;
    private String commit;
    private Boolean isDefault;
}

