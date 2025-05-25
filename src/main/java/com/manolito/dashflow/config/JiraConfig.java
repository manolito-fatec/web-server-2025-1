package com.manolito.dashflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jira")
@Getter
@Setter
public class JiraConfig {
    private String email;
    private String token;

    public String getEmail() {
        return email != null ? email : System.getenv("JIRA_EMAIL");
    }

    public String getToken() {
        return token != null ? token : System.getenv("JIRA_TOKEN");
    }
}