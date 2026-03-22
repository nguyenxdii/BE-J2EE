package com.j2ee.carbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing // Bật để @CreatedDate và @LastModifiedDate tự động hoạt động
public class CarbookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarbookingApplication.class, args);
	}

}

