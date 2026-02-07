package fr.iandeveseleer.cicddashboardapi.repository.event;

import fr.iandeveseleer.cicddashboardapi.repository.ProjectVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
@RequiredArgsConstructor
public class ProjectEventHandler {

    private final ProjectVersionRepository projectVersionRepository;

//    @HandleAfterCreate
//    public void createInitialVersion(Project project) {
//        ProjectVersion version = new ProjectVersion();
//        version.setProject(project);
//        version.setVersion(1);
//        projectVersionRepository.save(version);
//    }
}

