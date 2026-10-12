package com.panacea.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class PanaceaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PanaceaBackendApplication.class, args);
	}

}
