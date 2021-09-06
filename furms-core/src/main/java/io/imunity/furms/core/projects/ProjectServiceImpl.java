/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.projects.CreateProjectEvent;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.RemoveProjectEvent;
import io.imunity.furms.domain.projects.UpdateProjectEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserProjectMembershipEvent;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
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

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LEAVE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.PROJECT_USER;

@Service
class ProjectServiceImpl implements ProjectService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectRepository projectRepository;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final UsersDAO usersDAO;
	private final ProjectServiceValidator validator;
	private final AuthzService authzService;
	private final UserOperationService userOperationService;
	private final ApplicationEventPublisher publisher;
	private final ProjectInstallationService projectInstallationService;


	public ProjectServiceImpl(ProjectRepository projectRepository, ProjectGroupsDAO projectGroupsDAO, UsersDAO usersDAO,
	                          ProjectServiceValidator validator, ApplicationEventPublisher publisher,
	                          AuthzService authzService, UserOperationService userOperationService,
	                          ProjectInstallationService projectInstallationService) {
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.usersDAO = usersDAO;
		this.validator = validator;
		this.publisher = publisher;
		this.authzService = authzService;
		this.projectInstallationService = projectInstallationService;
		this.userOperationService = userOperationService;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public boolean existsById(String id) {
		return projectRepository.exists(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id")
	public Optional<Project> findById(String id) {
		return projectRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<Project> findAllByCommunityId(String communityId) {
		return projectRepository.findAllByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public Set<Project> findAllNotExpiredByCommunityId(String communityId) {
		return projectRepository.findAllNotExpiredByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT)
	public Set<Project> findAll() {
		return projectRepository.findAll();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public boolean isProjectInTerminalState(String projectId) {
		return projectInstallationService.isProjectInTerminalState(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public boolean isProjectInTerminalState(String communityId, String projectId) {
		return projectInstallationService.isProjectInTerminalState(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id")
	public boolean isProjectExpired(String id) {
		final Optional<Project> project = findById(id);
		return project.isEmpty() || project.get().isExpired();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "project.communityId")
	public String create(Project project) {
		validator.validateCreate(project);
		String id = projectRepository.create(project);
		projectGroupsDAO.create(new ProjectGroup(id, project.getName(), project.getCommunityId()));
		addAdmin(project.getCommunityId(), id, project.getLeaderId());
		publisher.publishEvent(new CreateProjectEvent(project.getId()));
		LOG.info("Project with given ID: {} was created: {}", id, project);
		return id;
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "project.id")
	public void update(Project project) {
		validator.validateUpdate(project);
		projectRepository.update(project);
		projectGroupsDAO.update(new ProjectGroup(project.getId(), project.getName(), project.getCommunityId()));
		addAdmin(project.getCommunityId(), project.getId(), project.getLeaderId());
		updateInAgent(project);
		publisher.publishEvent(new UpdateProjectEvent(project.getId()));
		LOG.info("Project was updated {}", project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "attributes.id")
	public void update(ProjectAdminControlledAttributes attributes) {
		validator.validateLimitedUpdate(attributes);
		Project project = projectRepository.findById(attributes.getId())
				.orElseThrow(() -> new IllegalStateException("Project not found: " + attributes.getId()));
		Project updatedProject = Project.builder()
			.id(project.getId())
			.communityId(project.getCommunityId())
			.name(project.getName())
			.acronym(project.getAcronym())
			.researchField(attributes.getResearchField())
			.utcStartTime(project.getUtcStartTime())
			.utcEndTime(project.getUtcEndTime())
			.leaderId(project.getLeaderId())
			.description(attributes.getDescription())
			.logo(attributes.getLogo())
			.build();
		projectRepository.update(updatedProject);
		projectGroupsDAO.update(new ProjectGroup(updatedProject.getId(), updatedProject.getName(), updatedProject.getCommunityId()));
		updateInAgent(project);
		publisher.publishEvent(new UpdateProjectEvent(project.getId()));
		LOG.info("Project was updated {}", attributes);
	}

	private void updateInAgent(Project project) {
		projectInstallationService.update(project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId")
	public void delete(String projectId, String communityId) {
		validator.validateDelete(projectId);
		List<FURMSUser> allProjectUsers = projectGroupsDAO.getAllUsers(communityId, projectId);
		removeFromAgent(projectId);
		projectRepository.delete(projectId);
		projectGroupsDAO.delete(communityId, projectId);
		publisher.publishEvent(new RemoveProjectEvent(projectId, allProjectUsers));
		LOG.info("Project with given ID: {} was deleted", projectId);
	}

	private void removeFromAgent(String projectId) {
		projectInstallationService.remove(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public List<FURMSUser> findAllAdmins(String communityId, String projectId){
		return projectGroupsDAO.getAllAdmins(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public boolean isAdmin(String projectId){
		return authzService.isResourceMember(projectId, PROJECT_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public void addAdmin(String communityId, String projectId, PersistentId userId){
		projectGroupsDAO.addProjectUser(communityId, projectId, userId, PROJECT_ADMIN);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public void inviteAdmin(String communityId, String projectId, PersistentId id){
		Optional<FURMSUser> user = usersDAO.findById(id);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email address.");
		}
		projectGroupsDAO.addProjectUser(communityId, projectId, user.get().id.orElse(null), PROJECT_ADMIN);
		publisher.publishEvent(new InviteUserEvent(user.get().id.orElse(null), new ResourceId(projectId, PROJECT)));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public void removeAdmin(String communityId, String projectId, PersistentId userId){
		projectGroupsDAO.removeAdmin(communityId, projectId, userId);
		publisher.publishEvent(new RemoveUserRoleEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId")
	public List<FURMSUser> findAllUsers(String communityId, String projectId){
		return projectGroupsDAO.getAllUsers(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId")
	public List<FURMSUser> findAllUsers(String communityId){
		return projectGroupsDAO.getAllUsers(communityId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public boolean isUser(String projectId) {
		return authzService.isResourceMember(projectId, PROJECT_USER);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void addUser(String communityId, String projectId, PersistentId userId){
		projectGroupsDAO.addProjectUser(communityId, projectId, userId, PROJECT_USER);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void inviteUser(String communityId, String projectId, PersistentId userId) {
		Optional<FURMSUser> user = usersDAO.findById(userId);
		if (user.isEmpty()) {
			throw new IllegalArgumentException("Could not invite user due to wrong email adress.");
		}
		projectGroupsDAO.addProjectUser(communityId, projectId, user.get().id.orElse(null), PROJECT_USER);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void removeUser(String communityId, String projectId, PersistentId userId){
		removeUserFromProject(communityId, projectId, userId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LEAVE, resourceType = PROJECT, id = "projectId")
	public void resignFromMembership(String communityId, String projectId) {
		final PersistentId userId = authzService.getCurrentUserId();
		removeUserFromProject(communityId, projectId, userId);
	}

	private void removeUserFromProject(String communityId, String projectId, PersistentId userId) {
		userOperationService.createUserRemovals(projectId, userId);
		projectGroupsDAO.removeUser(communityId, projectId, userId);
		publisher.publishEvent(new RemoveUserProjectMembershipEvent(userId, new ResourceId(projectId, PROJECT)));
	}
}
