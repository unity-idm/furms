/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

public class ProjectViewModelMapper {
	static ProjectViewModel map(Project project, FURMSUser projectLeader) {
		return ProjectViewModel.builder()
			.id(project.getId())
			.communityId(project.getCommunityId())
			.name(project.getName())
			.description(project.getDescription())
			.logo(project.getLogo())
			.acronym(project.getAcronym())
			.researchField(project.getResearchField())
			.startTime(project.getStartTime())
			.endTime(project.getEndTime())
			.projectLeader(projectLeader == null ? FurmsViewUserModel.EMPTY : new FurmsViewUserModel(projectLeader))
			.build();
	}

	public static Project map(ProjectViewModel project){
		return Project.builder()
			.id(project.id)
			.communityId(project.communityId)
			.name(project.name)
			.description(project.description)
			.logo(project.logo)
			.acronym(project.acronym)
			.researchField(project.researchField)
			.startTime(project.startTime)
			.endTime(project.endTime)
			.leaderId(project.projectLeader.id)
			.build();
	}
}
