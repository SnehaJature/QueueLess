package com.queueless.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BusinessServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessServiceApplication.class, args);
    }
}
