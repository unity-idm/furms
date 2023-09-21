/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.unity.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.rest.api.types.basic.RestAttribute;
import io.imunity.rest.api.types.basic.RestAttributeExt;

import java.util.Map;
import java.util.function.Function;

public class AttributeValueMapper {
	private static final Function<String, String> DEFAULT_VALUE_MAPPER = value -> value;
	private static final Map<String, Function<String, String>> mappersByAttributeType = Map.of(
				"verifiableEmail", AttributeValueMapper::convertEmail
			);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	public static String toFurmsAttributeValue(RestAttributeExt attribute, String value) {
		return mappersByAttributeType.getOrDefault(attribute.valueSyntax, DEFAULT_VALUE_MAPPER)
			.apply(value);
	}

	public static String toFurmsAttributeValue(RestAttribute attribute, String value) {
		return mappersByAttributeType.getOrDefault(attribute.valueSyntax, DEFAULT_VALUE_MAPPER)
			.apply(value);
	}
	
	private static String convertEmail(String encodedEmail) {
		try
		{
			return objectMapper.readTree(encodedEmail).get("value").asText();
		} catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}
}
