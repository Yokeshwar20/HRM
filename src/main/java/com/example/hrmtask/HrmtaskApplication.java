package com.example.hrmtask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HrmtaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(HrmtaskApplication.class, args);
	}

}
