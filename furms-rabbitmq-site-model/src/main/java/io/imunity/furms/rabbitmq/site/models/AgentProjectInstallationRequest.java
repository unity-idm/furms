/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.time.ZonedDateTime;
import java.util.Objects;

@JsonTypeName("AgentProjectInstallationRequest")
public class AgentProjectInstallationRequest implements Body {
	public final String identifier;
	public final String name;
	public final String description;
	public final String communityId;
	public final String communityName;
	public final String acronym;
	public final String researchField;
	public final ZonedDateTime validityStart;
	public final ZonedDateTime validityEnd;
	public final ProjectLeader projectLeader;

	@JsonCreator
	public AgentProjectInstallationRequest(String identifier, String name, String description, String communityId,
	                                String communityName, String acronym, String researchField, ZonedDateTime validityStart, ZonedDateTime validityEnd, ProjectLeader projectLeader) {
		this.identifier = identifier;
		this.name = name;
		this.description = description;
		this.communityId = communityId;
		this.communityName = communityName;
		this.acronym = acronym;
		this.researchField = researchField;
		this.validityStart = validityStart;
		this.validityEnd = validityEnd;
		this.projectLeader = projectLeader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentProjectInstallationRequest that = (AgentProjectInstallationRequest) o;
		return Objects.equals(identifier, that.identifier) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(communityName, that.communityName) &&
			Objects.equals(acronym, that.acronym) &&
			Objects.equals(researchField, that.researchField) &&
			Objects.equals(validityStart, that.validityStart) &&
			Objects.equals(validityEnd, that.validityEnd) &&
			Objects.equals(projectLeader, that.projectLeader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(identifier, name, description, communityId, communityName, acronym, researchField, validityStart, validityEnd, projectLeader);
	}

	@Override
	public String toString() {
		return "ProjectInstallationRequest{" +
			"id='" + identifier + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", communityId='" + communityId + '\'' +
			", communityName='" + communityName + '\'' +
			", acronym='" + acronym + '\'' +
			", researchField='" + researchField + '\'' +
			", validityStart=" + validityStart +
			", validityEnd=" + validityEnd +
			", projectLeader=" + projectLeader +
			'}';
	}

	public static ProjectInstallationRequestBuilder builder() {
		return new ProjectInstallationRequestBuilder();
	}

	public static final class ProjectInstallationRequestBuilder {
		private String identifier;
		private String name;
		private String description;
		private String communityId;
		private String communityName;
		private String acronym;
		private String researchField;
		private ZonedDateTime validityStart;
		private ZonedDateTime validityEnd;
		private ProjectLeader projectLeader;

		private ProjectInstallationRequestBuilder() {
		}

		public ProjectInstallationRequestBuilder identifier(String identifier) {
			this.identifier = identifier;
			return this;
		}

		public ProjectInstallationRequestBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectInstallationRequestBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectInstallationRequestBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectInstallationRequestBuilder communityName(String communityName) {
			this.communityName = communityName;
			return this;
		}

		public ProjectInstallationRequestBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}

		public ProjectInstallationRequestBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public ProjectInstallationRequestBuilder validityStart(ZonedDateTime validityStart) {
			this.validityStart = validityStart;
			return this;
		}

		public ProjectInstallationRequestBuilder validityEnd(ZonedDateTime validityEnd) {
			this.validityEnd = validityEnd;
			return this;
		}

		public ProjectInstallationRequestBuilder projectLeader(ProjectLeader projectLeader) {
			this.projectLeader = projectLeader;
			return this;
		}

		public AgentProjectInstallationRequest build() {
			return new AgentProjectInstallationRequest(identifier, name, description, communityId, communityName, acronym, researchField, validityStart, validityEnd, projectLeader);
		}
	}
}
