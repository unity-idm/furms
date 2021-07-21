/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

class PolicyAgreementParser {
	private static final ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.registerModule(new ParameterNamesModule());
	}

	static PolicyAgreementArgument parse(String jsonString){
		try {
			return objectMapper.readValue(jsonString, PolicyAgreementArgument.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}

	static String parse(PolicyAgreementArgument argument){
		try {
			return objectMapper.writeValueAsString(argument);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(e);
		}
	}
}
