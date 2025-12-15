package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.ProjectVersion;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "versions", path = "versions")
public interface ProjectVersionRepository extends PagingAndSortingRepository<ProjectVersion, Long>, CrudRepository<ProjectVersion, Long> {

}
