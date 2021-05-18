/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.time.LocalDateTime;
import java.util.Objects;

import io.imunity.furms.domain.users.FURMSUser;

public class ProjectInstallation {
	public final String id;
	public final String siteId;
	public final String siteExternalId;
	public final String name;
	public final String description;
	public final String communityId;
	public final String communityName;
	public final String acronym;
	public final String researchField;
	public final LocalDateTime validityStart;
	public final LocalDateTime validityEnd;
	public final FURMSUser leader;

	ProjectInstallation(String id,
			String siteId,
			String siteExternalId,
			String name,
			String description,
			String communityId,
			String communityName,
			String acronym,
			String researchField,
			LocalDateTime validityStart,
			LocalDateTime validityEnd,
			FURMSUser leader) {
		this.id = id;
		this.siteId = siteId;
		this.siteExternalId = siteExternalId;
		this.name = name;
		this.description = description;
		this.communityId = communityId;
		this.communityName = communityName;
		this.acronym = acronym;
		this.researchField = researchField;
		this.validityStart = validityStart;
		this.validityEnd = validityEnd;
		this.leader = leader;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallation that = (ProjectInstallation) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteExternalId, that.siteExternalId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(communityName, that.communityName) &&
			Objects.equals(acronym, that.acronym) &&
			Objects.equals(researchField, that.researchField) &&
			Objects.equals(validityStart, that.validityStart) &&
			Objects.equals(validityEnd, that.validityEnd) &&
			Objects.equals(leader, that.leader);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, siteExternalId, name, description, communityId, communityName, acronym,
				researchField, validityStart, validityEnd, leader);
	}

	@Override
	public String toString() {
		return "ProjectInstallation{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", siteExternalId='" + siteExternalId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", communityId='" + communityId + '\'' +
			", communityName='" + communityName + '\'' +
			", acronym='" + acronym + '\'' +
			", researchField='" + researchField + '\'' +
			", validityStart=" + validityStart +
			", validityEnd=" + validityEnd +
			", leader=" + leader +
			'}';
	}

	public static ProjectInstallationBuilder builder() {
		return new ProjectInstallationBuilder();
	}

	public static final class ProjectInstallationBuilder {
		private String id;
		private String siteId;
		private String siteExternalId;
		private String name;
		private String description;
		private String communityId;
		private String communityName;
		private String acronym;
		private String researchField;
		private LocalDateTime validityStart;
		private LocalDateTime validityEnd;
		private FURMSUser leader;

		private ProjectInstallationBuilder() {
		}

		public ProjectInstallationBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectInstallationBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationBuilder siteExternalId(String siteExternalId) {
			this.siteExternalId = siteExternalId;
			return this;
		}

		public ProjectInstallationBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectInstallationBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectInstallationBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectInstallationBuilder communityName(String communityName) {
			this.communityName = communityName;
			return this;
		}

		public ProjectInstallationBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}

		public ProjectInstallationBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public ProjectInstallationBuilder validityStart(LocalDateTime validityStart) {
			this.validityStart = validityStart;
			return this;
		}

		public ProjectInstallationBuilder validityEnd(LocalDateTime validityEnd) {
			this.validityEnd = validityEnd;
			return this;
		}

		public ProjectInstallationBuilder leader(FURMSUser leader) {
			this.leader = leader;
			return this;
		}

		public ProjectInstallation build() {
			return new ProjectInstallation(id, siteId, siteExternalId, name, description, communityId, communityName,
					acronym, researchField, validityStart, validityEnd, leader);
		}
	}
}
