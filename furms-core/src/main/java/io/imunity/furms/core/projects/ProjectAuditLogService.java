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
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class ProjectAuditLogService {

	private final AuthzService authzService;
	private final UsersDAO usersDAO;
	private final ApplicationEventPublisher publisher;
	private final ObjectMapper objectMapper;

	ProjectAuditLogService(AuthzService authzService, UsersDAO usersDAO, ApplicationEventPublisher publisher, ObjectMapper objectMapper) {
		this.authzService = authzService;
		this.usersDAO = usersDAO;
		this.publisher = publisher;
		this.objectMapper = objectMapper;
	}

	@EventListener
	void onProjectCreatedEvent(ProjectCreatedEvent event) {
		FURMSUser currentAuthNUser = authzService.getCurrentAuthNUser();
		AuditLog auditLog = AuditLog.builder()
			.resourceId(event.project.getId())
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
			.resourceId(event.project.getId())
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
			.resourceId(event.newProject.getId())
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
		json.put("leaderId", usersDAO.findById(project.getLeaderId()).map(usr -> usr.email).orElse(null));

		try {
			return objectMapper.writeValueAsString(json);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project with id %s cannot be parse", project.getId()), e);
		}
	}

	private String toJsonDiff(Project oldProject, Project newProject) {
		Map<String, Object> diffs = new HashMap<>();
		if(!Objects.equals(oldProject.getName(), newProject.getName()))
			diffs.put("name", newProject.getName());
		if(!Objects.equals(oldProject.getDescription(), newProject.getDescription()))
			diffs.put("description", newProject.getDescription());
		if(!Objects.equals(oldProject.getAcronym(), newProject.getAcronym()))
			diffs.put("acronym", newProject.getAcronym());
		if(!Objects.equals(oldProject.getResearchField(), newProject.getResearchField()))
			diffs.put("researchField", newProject.getResearchField());
		if(!Objects.equals(oldProject.getUtcStartTime(), newProject.getUtcStartTime()))
			diffs.put("utcStartTime", newProject.getUtcStartTime());
		if(!Objects.equals(oldProject.getUtcEndTime(), newProject.getUtcEndTime()))
			diffs.put("utcEndTime", newProject.getUtcEndTime());
		if(!Objects.equals(oldProject.getLeaderId(), newProject.getLeaderId()))
			diffs.put("leaderId", usersDAO.findById(newProject.getLeaderId()).map(usr -> usr.email).orElse(null));
		if(!Objects.equals(oldProject.getLogo(), newProject.getLogo()))
			diffs.put("logo", "CHANGED");

		try {
			return objectMapper.writeValueAsString(diffs);
		} catch (JsonProcessingException e) {
			throw new AuditLogException(String.format("Project with id %s cannot be parse", oldProject.getId()), e);
		}
	}
}
