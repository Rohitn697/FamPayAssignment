package com.fampay.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FampayAssignmentApplication {

  public static void main(String[] args) {
    SpringApplication.run(FampayAssignmentApplication.class, args);
  }

}