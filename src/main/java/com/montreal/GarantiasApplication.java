package com.montreal;

import com.montreal.oauth.domain.repository.infrastructure.CustomJpaRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaRepositories(basePackages = "com.montreal", repositoryBaseClass = CustomJpaRepositoryImpl.class)
@SpringBootApplication
@EnableScheduling
public class GarantiasApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarantiasApplication.class, args);
    }

}
