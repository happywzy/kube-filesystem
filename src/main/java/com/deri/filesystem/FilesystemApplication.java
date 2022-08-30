package com.deri.filesystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class FilesystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilesystemApplication.class, args);
    }

}
