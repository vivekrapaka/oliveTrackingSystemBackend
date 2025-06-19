package com.olive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.olive.config.FileStorageProperties;
@SpringBootApplication
@EnableConfigurationProperties({ // NEW
		FileStorageProperties.class // NEW
})

public class TaskmanagerApplication {

	public static void main(String[] args) {

		SpringApplication.run(TaskmanagerApplication.class, args);
	}

}
