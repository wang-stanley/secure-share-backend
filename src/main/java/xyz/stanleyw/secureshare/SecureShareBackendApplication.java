package xyz.stanleyw.secureshare;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import xyz.stanleyw.secureshare.config.StorageProperties;
import xyz.stanleyw.secureshare.service.StorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@EnableScheduling
public class SecureShareBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureShareBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return args -> storageService.init();
	}
}
