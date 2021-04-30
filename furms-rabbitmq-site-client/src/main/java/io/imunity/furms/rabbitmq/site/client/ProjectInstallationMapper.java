/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectUpdateRequest;
import io.imunity.furms.rabbitmq.site.models.ProjectLeader;

import java.time.ZoneOffset;

import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class ProjectInstallationMapper {
	static AgentProjectInstallationRequest mapInstall(ProjectInstallation projectInstallation){
		FURMSUser user = projectInstallation.leader;
		return AgentProjectInstallationRequest.builder()
			.identifier(projectInstallation.id)
			.name(projectInstallation.name)
			.description(projectInstallation.description)
			.communityId(projectInstallation.communityId)
			.community(projectInstallation.communityName)
			.acronym(projectInstallation.acronym)
			.researchField(projectInstallation.researchField)
			.validityStart(convertToZoneTime(projectInstallation.validityStart, ZoneOffset.UTC))
			.validityEnd(convertToZoneTime(projectInstallation.validityEnd, ZoneOffset.UTC))
			.projectLeader(UserMapper.map(user))
			.build();
	}

	static AgentProjectUpdateRequest mapUpdate(ProjectInstallation projectInstallation){
		FURMSUser user = projectInstallation.leader;
		return AgentProjectUpdateRequest.builder()
			.identifier(projectInstallation.id)
			.name(projectInstallation.name)
			.description(projectInstallation.description)
			.researchField(projectInstallation.researchField)
			.validityStart(convertToZoneTime(projectInstallation.validityStart, ZoneOffset.UTC))
			.validityEnd(convertToZoneTime(projectInstallation.validityEnd, ZoneOffset.UTC))
			.projectLeader(ProjectLeader.builder()
				.fenixUserId(user.id.map(persistentId -> persistentId.id).orElse(null))
				.email(user.email)
				.firstName(user.firstName.orElse(null))
				.lastName(user.lastName.orElse(null))
				.build())
			.build();
	}
}
