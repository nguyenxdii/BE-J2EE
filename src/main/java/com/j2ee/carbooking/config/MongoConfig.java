package com.j2ee.carbooking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // EnableMongoAuditing đã khai báo ở đây và ở main class
    // để chắc chắn auditing hoạt động
}
