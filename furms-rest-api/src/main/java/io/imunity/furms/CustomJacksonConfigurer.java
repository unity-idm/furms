/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;

/**
 * Customization of default Spring boot Jackson configuration.<br>
 * Enables serialization of DTOs w/o getters/setters.
 */
@Configuration
class CustomJacksonConfigurer implements Jackson2ObjectMapperBuilderCustomizer {
	
	@Override
	public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
		jacksonObjectMapperBuilder.visibility(PropertyAccessor.FIELD, Visibility.ANY);
	}
}