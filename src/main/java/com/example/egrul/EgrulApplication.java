package com.example.egrul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class EgrulApplication {

	public static void main(String[] args) {
		SpringApplication.run(EgrulApplication.class, args);
	}

}
