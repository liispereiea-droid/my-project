package com.labsupplysystem.labbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.labsupplysystem.labbackend.mapper")
public class LabBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(LabBackendApplication.class, args);
    }

}
