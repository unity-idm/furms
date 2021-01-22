/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import io.imunity.furms.domain.images.FurmsImage;

import java.time.LocalDateTime;
import java.util.Optional;

class ProjectViewModel {
	public final String id;
	public final String communityId;
	public String name;
	public String description;
	public Optional<FurmsImage> logo = Optional.empty();
	public String acronym;
	public String researchField;
	public LocalDateTime startTime;
	public LocalDateTime endTime;

	private ProjectViewModel(String id, String communityId, String name, String description, Optional<FurmsImage> logo, String acronym, String researchField, LocalDateTime startTime, LocalDateTime endTime) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.acronym = acronym;
		this.researchField = researchField;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	ProjectViewModel(String communityId) {
		this.id = null;
		this.communityId = communityId;
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

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Optional<FurmsImage> getLogo() {
		return logo;
	}

	public void setLogo(Optional<FurmsImage> logo) {
		this.logo = logo;
	}

	public String getAcronym() {
		return acronym;
	}

	public void setAcronym(String acronym) {
		this.acronym = acronym;
	}

	public String getResearchField() {
		return researchField;
	}

	public void setResearchField(String researchField) {
		this.researchField = researchField;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public static ProjectViewModelBuilder builder(){
		return new ProjectViewModelBuilder();
	}

	public static final class ProjectViewModelBuilder {
		public String id;
		public String communityId;
		public String name;
		public String description;
		public Optional<FurmsImage> logo;
		public String acronym;
		public String researchField;
		public LocalDateTime startTime;
		public LocalDateTime endTime;

		private ProjectViewModelBuilder() {
		}

		public static ProjectViewModelBuilder aProjectViewModel() {
			return new ProjectViewModelBuilder();
		}

		public ProjectViewModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectViewModelBuilder communityId(String communityId) {
			this.communityId = communityId;
			return this;
		}

		public ProjectViewModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ProjectViewModelBuilder description(String description) {
			this.description = description;
			return this;
		}

		public ProjectViewModelBuilder logo(Optional<FurmsImage> logo) {
			this.logo = logo;
			return this;
		}

		public ProjectViewModelBuilder acronym(String acronym) {
			this.acronym = acronym;
			return this;
		}

		public ProjectViewModelBuilder researchField(String researchField) {
			this.researchField = researchField;
			return this;
		}

		public ProjectViewModelBuilder startTime(LocalDateTime startTime) {
			this.startTime = startTime;
			return this;
		}

		public ProjectViewModelBuilder endTime(LocalDateTime endTime) {
			this.endTime = endTime;
			return this;
		}

		public ProjectViewModel build() {
			return new ProjectViewModel(id, communityId, name, description, logo, acronym, researchField, startTime, endTime);
		}
	}
}