package fr.iandeveseleer.cicddashboardapi.model.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "full", types = Project.class)
public interface ProjectView {

	Long getId();

	String getName();

	String getRepositoryUrl();

	@Value("#{target.versions}")
	List<ProjectVersionView> getVersions();
}