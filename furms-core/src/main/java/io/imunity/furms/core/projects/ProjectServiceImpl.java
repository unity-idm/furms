/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectEvent;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.domain.users.UserEvent;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.utils.EventOperation.*;

@Service
class ProjectServiceImpl implements ProjectService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final UsersDAO usersDAO;
	private final ProjectServiceValidator validator;
	private final ApplicationEventPublisher publisher;


	public ProjectServiceImpl(ProjectRepository projectRepository, ProjectGroupsDAO projectGroupsDAO, UsersDAO usersDAO,
	                          ProjectServiceValidator validator, ApplicationEventPublisher publisher) {
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.usersDAO = usersDAO;
		this.validator = validator;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id")
	public Optional<Project> findById(String id) {
		return projectRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<Project> findAll(String communityId) {
		return projectRepository.findAll(communityId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<Project> findAll() {
		return projectRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = COMMUNITY, id = "project.communityId")
	public void create(Project project) {
		validator.validateCreate(project);
		String id = projectRepository.create(project);
		projectGroupsDAO.create(new ProjectGroup(id, project.getName(), project.getCommunityId()));
		usersDAO.addProjectAdminRole(project.getCommunityId(), id, project.getLeaderId());
		publisher.publishEvent(new ProjectEvent(project.getId(), CREATE));
		LOG.info("Project with given ID: {} was created: {}", id, project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "project.id")
	public void update(Project project) {
		validator.validateUpdate(project);
		projectRepository.update(project);
		projectGroupsDAO.update(new ProjectGroup(project.getId(), project.getName(), project.getCommunityId()));
		usersDAO.addProjectAdminRole(project.getCommunityId(), project.getId(), project.getLeaderId());
		publisher.publishEvent(new ProjectEvent(project.getId(), UPDATE));
		LOG.info("Project was updated {}", project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "attributes.id")
	public void update(ProjectAdminControlledAttributes attributes) {
		validator.validateLimitedUpdate(attributes);
		Project project = projectRepository.findById(attributes.getId()).get();
		Project updatedProject = Project.builder()
			.id(project.getId())
			.communityId(project.getCommunityId())
			.name(project.getName())
			.acronym(project.getAcronym())
			.researchField(project.getResearchField())
			.startTime(project.getStartTime())
			.endTime(project.getEndTime())
			.description(attributes.getDescription())
			.logo(attributes.getLogo())
			.build();
		projectRepository.update(updatedProject);
		projectGroupsDAO.update(new ProjectGroup(updatedProject.getId(), updatedProject.getName(), updatedProject.getCommunityId()));
		publisher.publishEvent(new ProjectEvent(project.getId(), UPDATE));
		LOG.info("Project was updated {}", attributes);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String projectId, String communityId) {
		validator.validateDelete(projectId);
		projectRepository.delete(projectId);
		projectGroupsDAO.delete(communityId, projectId);
		publisher.publishEvent(new ProjectEvent(projectId, DELETE));
		LOG.info("Project with given ID: {} was deleted", projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public List<User> findAllAdmins(String communityId, String projectId){
		return projectGroupsDAO.getAllAdmins(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public boolean isAdmin(String communityId, String projectId, String userId){
		return projectGroupsDAO.isAdmin(communityId, projectId, userId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void addAdmin(String communityId, String projectId, String userId){
		projectGroupsDAO.addAdmin(communityId, projectId, userId);
		publisher.publishEvent(new UserEvent(userId, CREATE));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void inviteAdmin(String communityId, String projectId, String email){
		Optional<User> user = usersDAO.findByEmail(email);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email address.");
		}
		projectGroupsDAO.addAdmin(communityId, projectId, user.get().id);
		publisher.publishEvent(new UserEvent(user.get().id, CREATE));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "projectId")
	public void removeAdmin(String communityId, String projectId, String userId){
		projectGroupsDAO.removeAdmin(communityId, projectId, userId);
		publisher.publishEvent(new UserEvent(userId, DELETE));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public List<User> findAllUsers(String communityId, String projectId){
		return projectGroupsDAO.getAllUsers(communityId, projectId);
	}

	// FIXME: constrained released do to usage of this API for authenticated only user.
	//        Do we need a separate method to validate currently authN user against project?
	//        So similar like below but w/o last parameter that shall be taken from the context.
	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public boolean isUser(String communityId, String projectId, String userId) {
		return projectGroupsDAO.isUser(communityId, projectId, userId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void addUser(String communityId, String projectId, String userId){
		projectGroupsDAO.addUser(communityId, projectId, userId);
		publisher.publishEvent(new UserEvent(userId, CREATE));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void inviteUser(String communityId, String projectId, String email) {
		Optional<User> user = usersDAO.findByEmail(email);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		projectGroupsDAO.addUser(communityId, projectId, user.get().id);
		publisher.publishEvent(new UserEvent(user.get().id, CREATE));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LEAVE, resourceType = PROJECT, id = "projectId")
	public void removeUser(String communityId, String projectId, String userId){
		projectGroupsDAO.removeUser(communityId, projectId, userId);
		publisher.publishEvent(new UserEvent(userId, DELETE));
	}
}
