package com.foodtraceability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodTraceabilityApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodTraceabilityApplication.class, args);
    }
}
