package com.xtm;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication(exclude={
//        org.activiti.spring.boot.SecurityAutoConfiguration.class,
//        SecurityAutoConfiguration.class
//})
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
