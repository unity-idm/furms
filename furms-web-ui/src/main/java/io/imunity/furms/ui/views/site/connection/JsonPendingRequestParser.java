/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.connection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import elemental.json.Json;

class JsonPendingRequestParser {
	private static final ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	static String parseOperation(String json){
		return Json.parse(Json.parse(json).get("body").toJson()).keys()[0];
	}

	static String getPrettier(String json){
		try {
			return mapper.writeValueAsString(mapper.readTree(json));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException(String.format("Error parsing json: %s", json), e);
		}
	}
}
