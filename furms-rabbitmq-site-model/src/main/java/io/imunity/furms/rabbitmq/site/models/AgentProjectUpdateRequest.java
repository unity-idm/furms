/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.ZonedDateTime;
import java.util.Objects;

@JsonTypeName("ProjectUpdateRequest")
public class AgentProjectUpdateRequest implements Body {
	public final String identifier;
	public final String name;
	public final String description;
	public final String researchField;
	public final ZonedDateTime validityStart;
	public final ZonedDateTime validityEnd;
	public final AgentUser projectLeader;

	@JsonCreator
	AgentProjectUpdateRequest(String identifier, String name, String description, String researchField, ZonedDateTime validityStart, ZonedDateTime validityEnd, AgentUser projectLeader) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.researchField = researchField;
		this.validityStart = validityStart;
		this.validityEnd = validityEnd;
		this.projectLeader = projectLeader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectUpdateRequest that = (AgentProjectUpdateRequest) o;
		return Objects.equals(identifier, that.identifier) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(researchField, that.researchField) &&
			Objects.equals(validityStart, that.validityStart) &&
			Objects.equals(validityEnd, that.validityEnd) &&
			Objects.equals(projectLeader, that.projectLeader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, name, description, researchField, validityStart, validityEnd, projectLeader);
	}

	@Override
	public String toString() {
		return "AgentProjectUpdateRequest{" +
			"identifier='" + identifier + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", researchField='" + researchField + '\'' +
			", validityStart=" + validityStart +
			", validityEnd=" + validityEnd +
			", projectLeader=" + projectLeader +
			'}';
	}

	public static AgentProjectUpdateRequestBuilder builder() {
		return new AgentProjectUpdateRequestBuilder();
	}

	public static final class AgentProjectUpdateRequestBuilder {
		private String identifier;
		private String name;
		private String description;
		private String researchField;
		private ZonedDateTime validityStart;
		private ZonedDateTime validityEnd;
		private AgentUser projectLeader;

		private AgentProjectUpdateRequestBuilder() {
		}

		public AgentProjectUpdateRequestBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public AgentProjectUpdateRequestBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AgentProjectUpdateRequestBuilder description(String description) {
			this.description = description;
			return this;
		}

		public AgentProjectUpdateRequestBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public AgentProjectUpdateRequestBuilder validityStart(ZonedDateTime validityStart) {
			this.validityStart = validityStart;
			return this;
		}

		public AgentProjectUpdateRequestBuilder validityEnd(ZonedDateTime validityEnd) {
			this.validityEnd = validityEnd;
			return this;
		}

		public AgentProjectUpdateRequestBuilder projectLeader(AgentUser projectLeader) {
			this.projectLeader = projectLeader;
			return this;
		}

		public AgentProjectUpdateRequest build() {
			return new AgentProjectUpdateRequest(identifier, name, description, researchField, validityStart, validityEnd, projectLeader);
		}
	}
}
