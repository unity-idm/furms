/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.images.FurmsImage;

import java.time.LocalDateTime;

public class Project {

	private final String id;
	private final String communityId;
	private final String name;
	private final String description;
	private final FurmsImage logo;
	private final String acronym;
	private final String researchField;
	private final LocalDateTime start;
	private final LocalDateTime end;

	public Project(String id, String communityId, String name, String description, FurmsImage logo,
	               String acronym, String researchField, LocalDateTime start, LocalDateTime end) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.acronym = acronym;
		this.researchField = researchField;
		this.start = start;
		this.end = end;
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

	public LocalDateTime getStart() {
		return start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public static ProjectEntityBuilder builder() {
		return new ProjectEntityBuilder();
	}

	public static class ProjectEntityBuilder {
		protected String id;
		private String communityId;
		private String name;
		private String description;
		private FurmsImage logo;
		private String acronym;
		private String researchField;
		private LocalDateTime start;
		private LocalDateTime end;

		private ProjectEntityBuilder() {
		}

		public ProjectEntityBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectEntityBuilder community(String communityId) {
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

		public Project build() {
			return new Project(id, communityId, name, description, logo, acronym, researchField, start, end);
		}
	}
}
