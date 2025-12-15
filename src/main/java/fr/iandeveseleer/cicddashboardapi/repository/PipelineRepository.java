package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.Pipeline;
import fr.iandeveseleer.cicddashboardapi.model.entity.Project;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "pipelines", path = "pipelines")
public interface PipelineRepository extends PagingAndSortingRepository<Pipeline, Long>, CrudRepository<Pipeline, Long> {
    Pipeline findByCiId(Long ciId);
    Pipeline findByProjectVersionId(Long projectVersionId);
    List<Pipeline> findByProjectVersionProject(Project projectVersionProject);
}
