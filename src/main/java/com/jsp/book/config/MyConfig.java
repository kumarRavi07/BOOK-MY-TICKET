package com.jsp.book.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
public class MyConfig implements WebMvcConfigurer {

	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	@Bean
	SecureRandom secureRandom() {
		return new SecureRandom();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
		String uploadLocation = uploadPath.toUri().toString();
		if (!uploadLocation.endsWith("/")) {
			uploadLocation += "/";
		}
		registry.addResourceHandler("/uploads/**").addResourceLocations(uploadLocation);
	}
}
