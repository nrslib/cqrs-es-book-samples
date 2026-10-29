package org.example.cqrsesbooksamples.chapter02._2;

import org.example.cqrsesbooksamples.chapter02.config.FullyQualifiedBeanNameGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(nameGenerator = FullyQualifiedBeanNameGenerator.class)
public class UserCrudApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserCrudApplication.class, args);
    }
}
