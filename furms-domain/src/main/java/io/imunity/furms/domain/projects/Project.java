/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.utils.UTCTimeUtils;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {

	private final ProjectId id;
	private final CommunityId communityId;
	private final String name;
	private final String description;
	private final FurmsImage logo;
	private final String acronym;
	private final String researchField;
	private final LocalDateTime utcStartTime;
	private final LocalDateTime utcEndTime;
	private final PersistentId leaderId;

	public Project(ProjectId id, CommunityId communityId, String name, String description, FurmsImage logo, String acronym,
	               String researchField, LocalDateTime utcStartTime, LocalDateTime utcEndTime, PersistentId projectLeaderId) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.acronym = acronym;
		this.researchField = researchField;
		this.utcStartTime = utcStartTime;
		this.utcEndTime = utcEndTime;
		this.leaderId = projectLeaderId;
	}

	public ProjectId getId() {
		return id;
	}

	public CommunityId getCommunityId() {
		return communityId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public FurmsImage getLogo() {
		return logo;
	}

	public String getAcronym() {
		return acronym;
	}

	public String getResearchField() {
		return researchField;
	}

	public LocalDateTime getUtcStartTime() {
		return utcStartTime;
	}

	public LocalDateTime getUtcEndTime() {
		return utcEndTime;
	}

	public PersistentId getLeaderId() {
		return leaderId;
	}

	public boolean isExpired() {
		return UTCTimeUtils.isExpired(utcEndTime);
	}

	public static ProjectBuilder builder() {
		return new ProjectBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Project project = (Project) o;
		return Objects.equals(id, project.id) &&
			Objects.equals(communityId, project.communityId) &&
			Objects.equals(name, project.name) &&
			Objects.equals(description, project.description) &&
			Objects.equals(logo, project.logo) &&
			Objects.equals(acronym, project.acronym) &&
			Objects.equals(researchField, project.researchField) &&
			Objects.equals(utcStartTime, project.utcStartTime) &&
			Objects.equals(utcEndTime, project.utcEndTime) &&
			Objects.equals(leaderId, project.leaderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, logo, acronym, researchField, utcStartTime, utcEndTime, leaderId);
	}

	@Override
	public String toString() {
		return "Project{" +
			"id='" + id + '\'' +
			", communityId='" + communityId + '\'' +
			", name='" + name + '\'' +
			", description='" + description + '\'' +
			", acronym='" + acronym + '\'' +
			", researchField='" + researchField + '\'' +
			", startTime=" + utcStartTime +
			", endTime=" + utcEndTime +
			", projectLeaderId=" + leaderId +
			'}';
	}

	public static class ProjectBuilder {
		private ProjectId id;
		private CommunityId communityId;
		private String name;
		private String description;
		private FurmsImage logo;
		private String acronym;
		private String researchField;
		private LocalDateTime utcStartTime;
		private LocalDateTime utcEndTime;
		private PersistentId leaderId;

		private ProjectBuilder() {
		}

		public ProjectBuilder id(String id) {
			this.id = new ProjectId(id);
			return this;
		}

		public ProjectBuilder id(ProjectId id) {
			this.id = id;
			return this;
		}

		public ProjectBuilder communityId(String communityId) {
			this.communityId = new CommunityId(communityId);
			return this;
		}

		public ProjectBuilder communityId(CommunityId communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}

		public ProjectBuilder logo(byte[] logoImage, String logoType) {
			this.logo = new FurmsImage(logoImage, logoType);
			return this;
		}

		public ProjectBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}

		public ProjectBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public ProjectBuilder utcStartTime(LocalDateTime start) {
			this.utcStartTime = start;
			return this;
		}

		public ProjectBuilder utcEndTime(LocalDateTime end) {
			this.utcEndTime = end;
			return this;
		}

		public ProjectBuilder leaderId(PersistentId projectLeaderId) {
			this.leaderId = projectLeaderId;
			return this;
		}

		public Project build() {
			return new Project(id, communityId, name, description, logo, acronym, researchField, utcStartTime, utcEndTime, leaderId);
		}
	}
}
