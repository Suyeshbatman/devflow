package com.devflow.userservice;

// [IMPORT] @TestConfiguration is an ANNOTATION: a @Configuration that only exists for tests.
import org.springframework.boot.test.context.TestConfiguration;
// [IMPORT] @ServiceConnection is an ANNOTATION: "connect the app to this container automatically".
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
// [IMPORT] @Bean ANNOTATION
import org.springframework.context.annotation.Bean;
// [IMPORT] PostgreSQLContainer is a CLASS from Testcontainers that controls a Postgres Docker container.
import org.testcontainers.postgresql.PostgreSQLContainer;

//   proxyBeanMethods = false: a small startup optimization (Spring doesn't need to wrap this class).
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer(){
        return new PostgreSQLContainer("postgres:17");
    }
}
