package com.manolito.dashflow;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class DashflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(DashflowApplication.class, args);
	}

}
