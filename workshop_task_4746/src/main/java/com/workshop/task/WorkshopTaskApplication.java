package com.workshop.task;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@MapperScan("com.workshop.task.mapper")
public class WorkshopTaskApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkshopTaskApplication.class, args);
    }

}