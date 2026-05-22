package com.devflow.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = three annotations in one:
//   @Configuration     = this class can define Spring beans
//   @EnableAutoConfiguration = Spring Boot auto-configures
//     everything based on what's on the classpath
//     (sees PostgreSQL driver → auto-configures DataSource)
//     (sees Spring Security → auto-configures security)
//   @ComponentScan     = scans com.devflow.user and all
//     sub-packages for @Service, @Repository, @Controller etc.
@SpringBootApplication
public class UserServiceApplication {

    public static void main(String[] args) {
        // SpringApplication.run() bootstraps the entire app:
        // 1. Creates the Spring ApplicationContext (IoC container)
        // 2. Registers all beans (@Service, @Repository etc.)
        // 3. Starts embedded Tomcat on port 8081
        // 4. Applies all auto-configurations
        SpringApplication.run(UserServiceApplication.class, args);
    }
}