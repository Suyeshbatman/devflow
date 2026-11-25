package com.devflow.marketplace;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MarketplaceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarketplaceServiceApplication.class, args);
	}

}
