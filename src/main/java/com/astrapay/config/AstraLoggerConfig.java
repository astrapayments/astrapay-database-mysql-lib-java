package com.astrapay.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

import com.astrapay.logger.AstraLogger;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AstraLoggerConfig {

	@Bean
	@Primary
	AstraLogger getAstraLogger() {

		return new AstraLogger();

	}

}
