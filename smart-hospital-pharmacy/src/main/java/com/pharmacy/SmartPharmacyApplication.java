package com.pharmacy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartPharmacyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartPharmacyApplication.class, args);
    }
}
