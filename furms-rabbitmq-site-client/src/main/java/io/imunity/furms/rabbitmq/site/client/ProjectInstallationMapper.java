/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequest;

import java.time.ZoneOffset;

import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class ProjectInstallationMapper {
	static AgentProjectInstallationRequest map(ProjectInstallation projectInstallation){
		FURMSUser user = projectInstallation.leader;
		return AgentProjectInstallationRequest.builder()
			.identifier(projectInstallation.id.id.toString())
			.name(projectInstallation.name)
			.description(projectInstallation.description)
			.communityId(projectInstallation.communityId.id.toString())
			.community(projectInstallation.communityName)
			.acronym(projectInstallation.acronym)
			.researchField(projectInstallation.researchField)
			.validityStart(projectInstallation.validityStart.atOffset(ZoneOffset.UTC))
			.validityEnd(projectInstallation.validityEnd.atOffset(ZoneOffset.UTC))
			.projectLeader(UserMapper.map(user))
			.build();
	}

	static AgentProjectUpdateRequest map(Project project, FURMSUser user){
		return AgentProjectUpdateRequest.builder()
			.identifier(project.getId().id.toString())
			.name(project.getName())
			.description(project.getDescription())
			.researchField(project.getResearchField())
			.validityStart(convertToZoneTime(project.getUtcStartTime(), ZoneOffset.UTC).toOffsetDateTime())
			.validityEnd(convertToZoneTime(project.getUtcEndTime(), ZoneOffset.UTC).toOffsetDateTime())
			.projectLeader(UserMapper.map(user))
			.acronym(project.getAcronym())
			.build();
	}
}
