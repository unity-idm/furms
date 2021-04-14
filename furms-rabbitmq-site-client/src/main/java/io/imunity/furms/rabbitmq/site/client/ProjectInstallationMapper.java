/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.ProjectLeader;

class ProjectInstallationMapper {
	static AgentProjectInstallationRequest map(ProjectInstallation projectInstallation){
		FURMSUser user = projectInstallation.leader;
		return AgentProjectInstallationRequest.builder()
			.id(projectInstallation.id)
			.name(projectInstallation.name)
			.description(projectInstallation.description)
			.communityId(projectInstallation.communityId)
			.communityName(projectInstallation.communityName)
			.acronym(projectInstallation.acronym)
			.researchField(projectInstallation.researchField)
			.validityStart(projectInstallation.validityStart)
			.validityEnd(projectInstallation.validityEnd)
			.projectLeader(ProjectLeader.builder()
				.fenixUserId(user.id.map(persistentId -> persistentId.id).orElse(null))
				.email(user.email)
				.firstName(user.firstName.orElse(null))
				.lastName(user.lastName.orElse(null))
				.build())
			.build();
	}
}
