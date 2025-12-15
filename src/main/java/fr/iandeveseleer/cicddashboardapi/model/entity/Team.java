package fr.iandeveseleer.cicddashboardapi.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "teams")
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String channelUrl;
    @OneToMany(mappedBy = "team")
    private List<ProjectVersion> projectVersions;
}
