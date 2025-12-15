package fr.iandeveseleer.cicddashboardapi.model.entity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "full", types = ProjectVersion.class)
public interface ProjectVersionView {

	Long getId();

	Integer getVersion();

	@Value("#{target.team}")
	Team getTeam();

	@Value("#{target.pipelines}")
	List<PipelineView> getPipelines();
}