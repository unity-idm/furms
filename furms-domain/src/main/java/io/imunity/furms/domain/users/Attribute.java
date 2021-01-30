/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.List;

public class Attribute {
	public final String name;
	public final List<String> values;

	public Attribute(String name, List<String> values) {
		this.name = name;
		this.values = List.copyOf(values);
	}
}
