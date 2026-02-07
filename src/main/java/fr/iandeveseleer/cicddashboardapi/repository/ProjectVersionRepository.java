package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.ProjectVersion;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "versions", path = "versions")
public interface ProjectVersionRepository extends PagingAndSortingRepository<ProjectVersion, Long>, CrudRepository<ProjectVersion, Long> {

    @RestResource(path = "findByProjectRepositoryIdAndBranchId", rel = "findByProjectRepositoryIdAndBranchId")
    @Query("SELECT pv FROM ProjectVersion pv WHERE pv.project.repositoryId = :repositoryId AND pv.branchId = :branchId")
    List<ProjectVersion> findByProjectRepositoryIdAndBranchId(@Param("repositoryId") Long repositoryId, @Param("branchId") String branchId);

    @RestResource(path = "findByProjectRepositoryId", rel = "findByProjectRepositoryId")
    @Query("SELECT pv FROM ProjectVersion pv WHERE pv.project.repositoryId = :repositoryId")
    List<ProjectVersion> findByProjectRepositoryId(@Param("repositoryId") Long repositoryId);
}
