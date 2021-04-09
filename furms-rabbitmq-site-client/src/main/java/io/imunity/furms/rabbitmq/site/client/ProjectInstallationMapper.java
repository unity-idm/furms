/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;

class ProjectInstallationMapper {

	public ProjectInstallationRequest map(Community community, Project project, FURMSUser user){
		return ProjectInstallationRequest.builder()
			.id(project.getId())
			.name(project.getName())
			.description(project.getDescription())
			.communityId(community.getId())
			.communityName(community.getName())
			.acronym(project.getAcronym())
			.researchField(project.getResearchField())
			.validityStart(project.getUtcStartTime())
			.validityEnd(project.getUtcEndTime())
			.projectLeader(ProjectLeader.builder()
				.fenixUserId(user.id.map(persistentId -> persistentId.id).orElse(null))
				.email(user.email)
				.firstName(user.firstName.orElse(null))
				.lastName(user.lastName.orElse(null))
				.build())
			.build();
	}
}
