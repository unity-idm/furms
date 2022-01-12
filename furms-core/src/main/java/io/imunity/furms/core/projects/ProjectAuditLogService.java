/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.AuditLogException;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectCreatedEvent;
import io.imunity.furms.domain.projects.ProjectRemovedEvent;
import io.imunity.furms.domain.projects.ProjectUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class ProjectAuditLogService {

	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	ProjectAuditLogService(AuthzService authzService, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onProjectCreatedEvent(ProjectCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.CREATE)
			.operationCategory(Operation.PROJECTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.project.getName())
			.dataJson(toJson(event.project))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onProjectRemovedEvent(ProjectRemovedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.DELETE)
			.operationCategory(Operation.PROJECTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.project.getName())
			.dataJson(toJson(event.project))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	@EventListener
	void onProjectUpdatedEvent(ProjectUpdatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.originator(currentAuthNUser)
			.action(Action.UPDATE)
			.operationCategory(Operation.PROJECTS_MANAGEMENT)
			.utcTimestamp(convertToUTCTime(ZonedDateTime.now()))
			.operationSubject(event.newProject.getName())
			.dataJson(toJsonDiff(event.oldProject, event.newProject))
			.build();
		publisher.publishEvent(new AuditLogEvent(auditLog));
	}

	private String toJson(Project project) {
		Map<String, Object> json = new HashMap<>();
		json.put("id", project.getId());
		json.put("communityId", project.getCommunityId());
		json.put("name", project.getName());
		json.put("researchField", project.getResearchField());
		json.put("utcStartTime", project.getUtcStartTime());
		json.put("utcEndTime", project.getUtcEndTime());
		json.put("leaderId", project.getLeaderId());

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project with id %s cannot be parse", project.getId()), e);
		}
	}

	private String toJsonDiff(Project oldProject, Project newProject) {
		Map<String, Object> diffs = new HashMap<>();
		if(!oldProject.getName().equals(newProject.getName()))
			diffs.put("name", newProject.getName());
		if(!oldProject.getDescription().equals(newProject.getDescription()))
			diffs.put("description", newProject.getDescription());
		if(!oldProject.getAcronym().equals(newProject.getAcronym()))
			diffs.put("acronym", newProject.getAcronym());
		if(!oldProject.getResearchField().equals(newProject.getResearchField()))
			diffs.put("researchField", newProject.getResearchField());
		if(!oldProject.getUtcStartTime().equals(newProject.getUtcStartTime()))
			diffs.put("utcStartTime", newProject.getUtcStartTime());
		if(!oldProject.getUtcEndTime().equals(newProject.getUtcEndTime()))
			diffs.put("utcEndTime", newProject.getUtcEndTime());
		if(!oldProject.getLeaderId().equals(newProject.getLeaderId()))
			diffs.put("leaderId", newProject.getLeaderId());
		if(!oldProject.getLogo().equals(newProject.getLogo()))
			diffs.put("logo", "changed");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project with id %s cannot be parse", oldProject.getId()), e);
		}
	}
}
