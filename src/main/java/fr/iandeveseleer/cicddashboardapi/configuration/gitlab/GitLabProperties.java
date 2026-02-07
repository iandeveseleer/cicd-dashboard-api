package fr.iandeveseleer.cicddashboardapi.configuration.gitlab;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gitlab")
@Getter
@Setter
public class GitLabProperties {
    private String url;
    private String token;
}

