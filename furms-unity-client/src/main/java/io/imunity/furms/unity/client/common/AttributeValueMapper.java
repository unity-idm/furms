/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.unity.client.common;

import java.util.Map;
import java.util.function.Function;

import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.VerifiableEmail;

public class AttributeValueMapper {
	private static final Function<String, String> DEFAULT_VALUE_MAPPER = value -> value;
	private static final Map<String, Function<String, String>> mappersByAttributeType = Map.of(
				"verifiableEmail", AttributeValueMapper::convertEmail
			);
	
	public static String toFurmsAttributeValue(Attribute attribute, String value) {
		return mappersByAttributeType.getOrDefault(attribute.getValueSyntax(), DEFAULT_VALUE_MAPPER)
				.apply(value);
	}
	
	private static String convertEmail(String encodedEmail) {
		return VerifiableEmail.fromJsonString(encodedEmail).getValue();
	}
}
