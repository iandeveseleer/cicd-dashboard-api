package fr.iandeveseleer.cicddashboardapi.configuration.gitlab;

import lombok.RequiredArgsConstructor;
import org.gitlab4j.api.GitLabApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GitLabApiConfiguration {

    private final GitLabProperties gitLabProperties;

    @Bean
    public GitLabApi gitLabApi() {
        return new GitLabApi(gitLabProperties.getUrl(), gitLabProperties.getToken());
    }
}

