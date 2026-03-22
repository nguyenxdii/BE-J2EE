package com.j2ee.carbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling     // Bật @Scheduled cho scheduler đóng suất cọc
public class CarbookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarbookingApplication.class, args);
    }
}