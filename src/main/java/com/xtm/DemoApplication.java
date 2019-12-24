package com.xtm;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//@SpringBootApplication(exclude={
//        org.activiti.spring.boot.SecurityAutoConfiguration.class,
//        SecurityAutoConfiguration.class
//})
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }


    @Bean(name = "shiroDialect")
    public ShiroDialect shiroDialect(){
        return new ShiroDialect();
    }

}
