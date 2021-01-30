/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import java.util.List;

import io.imunity.furms.domain.users.Attribute;

public class AttributeJson {
	public final String name;
	public final List<String> values;

	public AttributeJson(String name, List<String> values) {
		this.name = name;
		this.values = List.copyOf(values);
	}
	
	public AttributeJson(Attribute attribute) {
		this(attribute.name, attribute.values);
	}
}
