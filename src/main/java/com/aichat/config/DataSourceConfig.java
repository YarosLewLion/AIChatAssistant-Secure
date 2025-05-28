package com.aichat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    @Autowired
    private Environment env;

    public DataSourceConfig(Environment env) {
        this.env = env;
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl != null && databaseUrl.startsWith("postgres://")) {
            try {
                URI dbUri = new URI(databaseUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();
                System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
                System.setProperty("spring.datasource.username", username);
                System.setProperty("spring.datasource.password", password);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse DATABASE_URL: " + databaseUrl, e);
            }
        }
    }
}
