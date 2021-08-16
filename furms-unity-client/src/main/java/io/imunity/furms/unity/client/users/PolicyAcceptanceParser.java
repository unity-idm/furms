/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

class PolicyAcceptanceParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.registerModule(new ParameterNamesModule());
	}

	static PolicyAcceptanceArgument parse(String jsonString){
		try {
			return objectMapper.readValue(jsonString, PolicyAcceptanceArgument.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static String parse(PolicyAcceptanceArgument argument){
		try {
			return objectMapper.writeValueAsString(argument);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
