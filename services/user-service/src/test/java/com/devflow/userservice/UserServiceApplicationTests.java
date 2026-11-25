package com.devflow.userservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
// Use the Testcontainers database instead of the dev one.
@Import(TestcontainersConfiguration.class)
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
