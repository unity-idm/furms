/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.projects;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("PROJECT")
class ProjectEntity extends UUIDIdentifiable {

	private final UUID communityId;
	private final String name;
	private final String description;
	private final byte[] logoImage;
	private final String logoType;
	private final String acronym;
	private final String researchField;
	private final LocalDateTime startTime;
	private final LocalDateTime endTime;

	public ProjectEntity(UUID id, UUID communityId, String name, String description, byte[] logoImage, String logoType,
	                     String acronym, String researchField, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logoImage = logoImage;
		this.logoType = logoType;
		this.acronym = acronym;
		this.researchField = researchField;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public Project toProject() {
		return Project.builder()
			.id(id.toString())
			.community(communityId)
			.name(name)
			.description(description)
			.logo(logoImage, logoType)
			.acronym(acronym)
			.researchField(researchField)
			.startTime(startTime)
			.endTime(endTime)
			.build();
	}

	public UUID getCommunityId() {
		return communityId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public byte[] getLogoImage() {
		return logoImage;
	}

	public String getLogoType() {
		return logoType;
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

	public static ProjectEntityBuilder builder() {
		return new ProjectEntityBuilder();
	}

	public static class ProjectEntityBuilder {
		protected UUID id;
		private UUID communityId;
		private String name;
		private String description;
		private byte[] logoImage;
		private String logoType;
		private String acronym;
		private String researchField;
		private LocalDateTime start;
		private LocalDateTime end;

		private ProjectEntityBuilder() {
		}

		public ProjectEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectEntityBuilder community(UUID communityId) {
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

		public ProjectEntityBuilder logo(FurmsImage furmsImage) {
			this.logoImage = furmsImage.getImage();
			this.logoType = furmsImage.getType();
			return this;
		}

		public ProjectEntityBuilder logo(byte[] logoImage, String logoType) {
			this.logoImage = logoImage;
			this.logoType = logoType;
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

		public ProjectEntity build() {
			return new ProjectEntity(
				id, communityId, name, description, logoImage, logoType, acronym, researchField, start, end
			);
		}
	}
}
