package com.recipe;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class RecommendedApplication {
    public static void main(String[] args) {

        Dotenv env = Dotenv.configure().ignoreIfMissing().load();
        env.entries().forEach((entry) -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
        SpringApplication.run(RecommendedApplication.class, args);
    }
}
