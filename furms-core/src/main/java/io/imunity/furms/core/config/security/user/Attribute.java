/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

class Attribute {
	public final List<String> values;
	public final long creationTs;
	public final long updateTs;
	public final boolean direct;
	public final String name;
	public final String groupPath;
	public final String valueSyntax;

	@JsonCreator
	Attribute(
			@JsonProperty("values") List<String> values,
			@JsonProperty("creationTs") long creationTs,
			@JsonProperty("updateTs") long updateTs,
			@JsonProperty("direct") boolean direct,
			@JsonProperty("name") String name,
			@JsonProperty("groupPath") String groupPath,
			@JsonProperty("valueSyntax") String valueSyntax) {
		this.values = values;
		this.creationTs = creationTs;
		this.updateTs = updateTs;
		this.direct = direct;
		this.name = name;
		this.groupPath = groupPath;
		this.valueSyntax = valueSyntax;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Attribute attribute = (Attribute) o;
		return creationTs == attribute.creationTs &&
			updateTs == attribute.updateTs &&
			direct == attribute.direct &&
			Objects.equals(values, attribute.values) &&
			Objects.equals(name, attribute.name) &&
			Objects.equals(groupPath, attribute.groupPath) &&
			Objects.equals(valueSyntax, attribute.valueSyntax);
	}

	@Override
	public int hashCode() {
		return Objects.hash(values, creationTs, updateTs, direct, name, groupPath, valueSyntax);
	}

	@Override
	public String toString() {
		return "Attribute{" +
			"values=" + values +
			", creationTs=" + creationTs +
			", updateTs=" + updateTs +
			", direct=" + direct +
			", name='" + name + '\'' +
			", groupPath='" + groupPath + '\'' +
			", valueSyntax='" + valueSyntax + '\'' +
			'}';
	}
}
