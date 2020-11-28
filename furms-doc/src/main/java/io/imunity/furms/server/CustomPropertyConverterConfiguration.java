/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.server;

import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;

/**
 * Customize swagger conversion logic to align with spring boot configuration.
 * 
 * The swagger and spring have its own configurations if it comes to json
 * conversion. The following implementation injects project's spring boot
 * specific customizations in this regards, into the swagger world.
 */
@Configuration
class CustomPropertyConverterConfiguration
{
	CustomPropertyConverterConfiguration(ObjectMapper mapper)
	{
		ModelConverters.getInstance().addConverter(new ModelResolver(mapper));
	}
}
