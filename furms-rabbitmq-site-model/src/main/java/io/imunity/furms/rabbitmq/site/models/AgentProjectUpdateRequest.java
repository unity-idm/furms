/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.OffsetDateTime;
import java.util.Objects;

@JsonTypeName("ProjectUpdateRequest")
public class AgentProjectUpdateRequest implements Body {
	public final String identifier;
	public final String name;
	public final String description;
	public final String researchField;
	public final String acronym;
	public final OffsetDateTime validityStart;
	public final OffsetDateTime validityEnd;
	public final AgentUser projectLeader;

	@JsonCreator
	AgentProjectUpdateRequest(String identifier, String name, String description, String researchField, 
			OffsetDateTime validityStart, OffsetDateTime validityEnd, AgentUser projectLeader, String acronym) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.researchField = researchField;
		this.validityStart = validityStart;
		this.validityEnd = validityEnd;
		this.projectLeader = projectLeader;
		this.acronym = acronym;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(acronym, description, identifier, name, projectLeader, researchField, validityEnd,
				validityStart);
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
		AgentProjectUpdateRequest other = (AgentProjectUpdateRequest) obj;
		return Objects.equals(acronym, other.acronym) && Objects.equals(description, other.description)
				&& Objects.equals(identifier, other.identifier) && Objects.equals(name, other.name)
				&& Objects.equals(projectLeader, other.projectLeader)
				&& Objects.equals(researchField, other.researchField)
				&& Objects.equals(validityEnd, other.validityEnd)
				&& Objects.equals(validityStart, other.validityStart);
	}

	@Override
	public String toString()
	{
		return String.format(
				"AgentProjectUpdateRequest [identifier=%s, name=%s, description=%s, researchField=%s, "
				+ "acronym=%s, validityStart=%s, validityEnd=%s, projectLeader=%s]",
				identifier, name, description, researchField, acronym, validityStart, validityEnd,
				projectLeader);
	}

	public static AgentProjectUpdateRequestBuilder builder() {
		return new AgentProjectUpdateRequestBuilder();
	}

	public static final class AgentProjectUpdateRequestBuilder {
		private String identifier;
		private String name;
		private String description;
		private String researchField;
		private OffsetDateTime validityStart;
		private OffsetDateTime validityEnd;
		private AgentUser projectLeader;
		private String acronym;

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

		public AgentProjectUpdateRequestBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}
		
		public AgentProjectUpdateRequestBuilder validityStart(OffsetDateTime validityStart) {
			this.validityStart = validityStart;
			return this;
		}

		public AgentProjectUpdateRequestBuilder validityEnd(OffsetDateTime validityEnd) {
			this.validityEnd = validityEnd;
			return this;
		}

		public AgentProjectUpdateRequestBuilder projectLeader(AgentUser projectLeader) {
			this.projectLeader = projectLeader;
			return this;
		}

		public AgentProjectUpdateRequest build() {
			return new AgentProjectUpdateRequest(identifier, name, description, researchField, 
					validityStart, validityEnd, projectLeader, acronym);
		}
	}
}
