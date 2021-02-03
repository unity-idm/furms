/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.images.FurmsImage;

import java.time.LocalDateTime;
import java.util.Objects;

public class Project {

	private final String id;
	private final String communityId;
	private final String name;
	private final String description;
	private final FurmsImage logo;
	private final String acronym;
	private final String researchField;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;
	private final String leaderId;

	public Project(String id, String communityId, String name, String description, FurmsImage logo, String acronym,
	               String researchField, LocalDateTime startTime, LocalDateTime endTime, String projectLeaderId) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.acronym = acronym;
		this.researchField = researchField;
		this.startTime = startTime;
		this.endTime = endTime;
		this.leaderId = projectLeaderId;
	}

	public String getId() {
		return id;
	}

	public String getCommunityId() {
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

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public String getLeaderId() {
		return leaderId;
	}

	public static ProjectEntityBuilder builder() {
		return new ProjectEntityBuilder();
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
			Objects.equals(startTime, project.startTime) &&
			Objects.equals(endTime, project.endTime) &&
			Objects.equals(leaderId, project.leaderId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityId, name, description, logo, acronym, researchField, startTime, endTime, leaderId);
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
			", startTime=" + startTime +
			", endTime=" + endTime +
			", projectLeaderId=" + leaderId +
			'}';
	}

	public static class ProjectEntityBuilder {
		private String id;
		private String communityId;
		private String name;
		private String description;
		private FurmsImage logo;
		private String acronym;
		private String researchField;
		private LocalDateTime start;
		private LocalDateTime end;
		private String leaderId;

		private ProjectEntityBuilder() {
		}

		public ProjectEntityBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectEntityBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectEntityBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectEntityBuilder logo(FurmsImage logo) {
			this.logo = logo;
			return this;
		}

		public ProjectEntityBuilder logo(byte[] logoImage, String logoType) {
			this.logo = new FurmsImage(logoImage, logoType);
			return this;
		}

		public ProjectEntityBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}

		public ProjectEntityBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public ProjectEntityBuilder startTime(LocalDateTime start) {
			this.start = start;
			return this;
		}

		public ProjectEntityBuilder endTime(LocalDateTime end) {
			this.end = end;
			return this;
		}

		public ProjectEntityBuilder leaderId(String projectLeaderId) {
			this.leaderId = projectLeaderId;
			return this;
		}

		public Project build() {
			return new Project(id, communityId, name, description, logo, acronym, researchField, start, end, leaderId);
		}
	}
}
