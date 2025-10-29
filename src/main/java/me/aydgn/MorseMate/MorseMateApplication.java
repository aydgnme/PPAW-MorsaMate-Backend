package me.aydgn.MorseMate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MorseMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(MorseMateApplication.class, args);
	}
}
