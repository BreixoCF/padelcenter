package com.bookings.padelcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PadelCenterApplication {

	public static void main(String[] args) {
		SpringApplication.run(PadelCenterApplication.class, args);
	}

}
