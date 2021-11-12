/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;
import java.util.Objects;

@JsonTypeName("ProjectInstallationResult")
public class AgentProjectInstallationResult implements Body, Result {
	public final Map<String, String> attributes;

	@JsonCreator
	public AgentProjectInstallationResult(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectInstallationResult that = (AgentProjectInstallationResult) o;
		return Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attributes);
	}

	@Override
	public String toString() {
		return "ProjectInstallationResult{" +
			", attributes=" + attributes +
			'}';
	}
}
