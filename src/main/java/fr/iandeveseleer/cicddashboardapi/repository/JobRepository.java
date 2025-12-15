package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.Job;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "jobs", path = "jobs")
public interface JobRepository extends PagingAndSortingRepository<Job, Long>, CrudRepository<Job, Long> {
    Job findByCiId(@Param("ciId") Long ciId);
    List<Job> findByPipelineId(@Param("pipelineId") Long pipelineId);
}
