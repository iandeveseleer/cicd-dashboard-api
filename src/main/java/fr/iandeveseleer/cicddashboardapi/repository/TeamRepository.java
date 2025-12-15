package fr.iandeveseleer.cicddashboardapi.repository;

import fr.iandeveseleer.cicddashboardapi.model.entity.Team;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "teams", path = "teams")
public interface TeamRepository extends PagingAndSortingRepository<Team, Long>, CrudRepository<Team, Long> {

}
