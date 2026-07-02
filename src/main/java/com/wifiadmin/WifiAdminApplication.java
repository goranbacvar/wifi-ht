package com.wifiadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WifiAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifiAdminApplication.class, args);
    }
}
