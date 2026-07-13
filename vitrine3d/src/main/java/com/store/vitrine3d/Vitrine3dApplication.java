package com.store.vitrine3d;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Vitrine3dApplication {

	public static void main(String[] args) {
		SpringApplication.run(Vitrine3dApplication.class, args);
	}

}
