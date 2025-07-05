package com.aizuda.snailjob.server.ui.config;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SnailJobAdminServerUiAutoConfiguration {

	@Bean
	public WebProperties webProperties() {
		WebProperties properties = new WebProperties();
		WebProperties.Resources resources = properties.getResources();
		resources.setStaticLocations(new String[]{"classpath:admin/"});
		return properties;
	}

}