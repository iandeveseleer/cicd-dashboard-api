package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.Project;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "projects", path = "projects")
public interface ProjectRepository extends PagingAndSortingRepository<Project, Long>, CrudRepository<Project, Long> {
    Optional<Project> findByRepositoryId(Long id);
}
