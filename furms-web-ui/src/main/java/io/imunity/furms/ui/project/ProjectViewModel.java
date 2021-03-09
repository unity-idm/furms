/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.time.LocalDateTime;
import java.util.Objects;

public class ProjectViewModel {
	public final String id;
	public final String communityId;
	public String name;
	public String description;
	public FurmsImage logo;
	public String acronym;
	public String researchField;
	public LocalDateTime startTime;
	public LocalDateTime endTime;
	public FurmsViewUserModel projectLeader;

	public ProjectViewModel(String id, String communityId, String name, String description, FurmsImage logo,
	                        String acronym, String researchField, LocalDateTime startTime, LocalDateTime endTime,
	                        FurmsViewUserModel projectLeader) {
		this.id = id;
		this.communityId = communityId;
		this.name = name;
		this.description = description;
		this.logo = logo;
		this.acronym = acronym;
		this.researchField = researchField;
		this.startTime = startTime;
		this.endTime = endTime;
		this.projectLeader = projectLeader;
	}

	public ProjectViewModel(String communityId) {
		this.id = null;
		this.communityId = communityId;
		this.logo = FurmsImage.empty();
	}

	public ProjectViewModel(ProjectViewModel projectViewModel) {
		this.id = projectViewModel.id;
		this.communityId = projectViewModel.communityId;
		this.name = projectViewModel.name;
		this.description = projectViewModel.description;
		this.logo = projectViewModel.logo;
		this.acronym = projectViewModel.acronym;
		this.researchField = projectViewModel.researchField;
		this.startTime = projectViewModel.startTime;
		this.endTime = projectViewModel.endTime;
		this.projectLeader = projectViewModel.projectLeader;
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

	public FurmsImage getLogo() {
		return logo;
	}

	public void setLogo(FurmsImage logo) {
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

	public FurmsViewUserModel getProjectLeader() {
		return projectLeader;
	}

	public void setProjectLeader(FurmsViewUserModel projectLeader) {
		this.projectLeader = projectLeader;
	}

	public static ProjectViewModelBuilder builder(){
		return new ProjectViewModelBuilder();
	}

	public boolean matches(String value) {
		return name.toLowerCase().contains(value) || 
				description.toLowerCase().contains(value) ||
				acronym.toLowerCase().contains(value) ||
				researchField.toLowerCase().contains(value);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectViewModel that = (ProjectViewModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	public boolean equalsFields(ProjectViewModel that) {
		if (this == that) return true;
		if (that == null) return false;
		return Objects.equals(id, that.id) &&
			Objects.equals(communityId, that.communityId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(description, that.description) &&
			Objects.equals(acronym, that.acronym) &&
			Objects.equals(researchField, that.researchField) &&
			Objects.equals(startTime, that.startTime) &&
			Objects.equals(endTime, that.endTime);
	}

	public static final class ProjectViewModelBuilder {
		public String id;
		public String communityId;
		public String name;
		public String description;
		public FurmsImage logo;
		public String acronym;
		public String researchField;
		public LocalDateTime startTime;
		public LocalDateTime endTime;
		public FurmsViewUserModel projectLeader;

		private ProjectViewModelBuilder() {
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

		public ProjectViewModelBuilder logo(FurmsImage logo) {
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

		public ProjectViewModelBuilder projectLeader(FurmsViewUserModel projectLeader) {
			this.projectLeader = projectLeader;
			return this;
		}

		public ProjectViewModel build() {
			return new ProjectViewModel(id, communityId, name, description, logo, acronym, researchField, startTime, endTime, projectLeader);
		}
	}
}
