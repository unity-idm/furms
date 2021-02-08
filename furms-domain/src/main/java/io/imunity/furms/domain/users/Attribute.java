/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import java.util.List;
import java.util.Objects;

public class Attribute {
	public final String name;
	public final List<String> values;

	public Attribute(String name, List<String> values) {
		this.name = name;
		this.values = List.copyOf(values);
	}

	public Attribute(String name, String value) {
		this(name, List.of(value));
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, values);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Attribute other = (Attribute) obj;
		return Objects.equals(name, other.name) && Objects.equals(values, other.values);
	}

	@Override
	public String toString()
	{
		return String.format("Attribute [name=%s, values=%s]", name, values);
	}
}
