/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

import java.util.Map;
import java.util.Objects;

@JsonDeserialize(builder = AgentProjectInstallationResult.AgentProjectInstallationResultBuilder.class)
@FurmsMessage(type = "ProjectInstallationResult")
public class AgentProjectInstallationResult {
	public final String identifier;
	public final Map<String, String> attributes;

	public AgentProjectInstallationResult(String identifier, Map<String, String> attributes) {
		this.identifier = identifier;
		this.attributes = attributes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectInstallationResult that = (AgentProjectInstallationResult) o;
		return Objects.equals(identifier, that.identifier) && Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, attributes);
	}

	@Override
	public String toString() {
		return "ProjectInstallationResult{" +
			"identifier='" + identifier + '\'' +
			", attributes=" + attributes +
			'}';
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentProjectInstallationResultBuilder {
		public String identifier;
		public Map<String, String> attributes;

		private AgentProjectInstallationResultBuilder() {
		}

		public static AgentProjectInstallationResultBuilder anAgentProjectInstallationResult() {
			return new AgentProjectInstallationResultBuilder();
		}

		public AgentProjectInstallationResultBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public AgentProjectInstallationResultBuilder attributes(Map<String, String> attributes) {
			this.attributes = attributes;
			return this;
		}

		public AgentProjectInstallationResult build() {
			return new AgentProjectInstallationResult(identifier, attributes);
		}
	}
}
