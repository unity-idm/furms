/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.projects.CreateProjectEvent;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.RemoveProjectEvent;
import io.imunity.furms.domain.projects.UpdateProjectEvent;
import io.imunity.furms.domain.users.AddUserEvent;
import io.imunity.furms.domain.users.FURMSUser;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
import static java.util.stream.Collectors.toSet;

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
	private final CapabilityCollector capabilityCollector;
	private final InvitatoryService invitatoryService;

	public ProjectServiceImpl(ProjectRepository projectRepository,
	                          ProjectGroupsDAO projectGroupsDAO,
	                          UsersDAO usersDAO,
	                          ProjectServiceValidator validator,
	                          ApplicationEventPublisher publisher,
	                          AuthzService authzService,
	                          UserOperationService userOperationService,
	                          ProjectInstallationService projectInstallationService,
	                          CapabilityCollector capabilityCollector,
	                          InvitatoryService invitatoryService) {
		this.projectRepository = projectRepository;
		this.projectGroupsDAO = projectGroupsDAO;
		this.usersDAO = usersDAO;
		this.validator = validator;
		this.publisher = publisher;
		this.authzService = authzService;
		this.projectInstallationService = projectInstallationService;
		this.userOperationService = userOperationService;
		this.capabilityCollector = capabilityCollector;
		this.invitatoryService = invitatoryService;
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT)
	public Set<Project> findAllByCurrentUserId() {
		final FURMSUser currentUser = authzService.getCurrentAuthNUser();
		return projectRepository.findAll().stream()
				.filter(project -> isProjectAdmin(project, currentUser.roles))
				.collect(toSet());
	}

	private boolean isProjectAdmin(Project project, Map<ResourceId, Set<Role>> roles) {
		final Set<Capability> capabilities = Set.of(PROJECT_READ, PROJECT_WRITE);
		return capabilityCollector.getCapabilities(roles, new ResourceId(project.getId(), PROJECT))
				.stream().anyMatch(capabilities::contains);
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
		publisher.publishEvent(new AddUserEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public Set<Invitation> findAllAdminsInvitations(String projectId) {
		return invitatoryService.getInvitations(PROJECT_ADMIN, UUID.fromString(projectId));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public Set<Invitation> findAllUsersInvitations(String projectId) {
		return invitatoryService.getInvitations(PROJECT_USER, UUID.fromString(projectId));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public void inviteAdmin(String projectId, PersistentId id){
		invitatoryService.inviteUser(id, new ResourceId(projectId, PROJECT), PROJECT_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId")
	public void inviteAdmin(String projectId, String email){
		invitatoryService.inviteUser(email, new ResourceId(projectId, PROJECT), PROJECT_ADMIN);
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
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId")
	public void addUser(String communityId, String projectId, PersistentId userId){
		projectGroupsDAO.addProjectUser(communityId, projectId, userId, PROJECT_USER);
		publisher.publishEvent(new AddUserEvent(userId, new ResourceId(projectId, PROJECT)));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void inviteUser(String projectId, PersistentId userId) {
		invitatoryService.inviteUser(userId, new ResourceId(projectId, PROJECT), PROJECT_USER);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void inviteUser(String projectId, String email) {
		invitatoryService.inviteUser(email, new ResourceId(projectId, PROJECT), PROJECT_USER);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void resendInvitation(String projectId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(projectId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", projectId, invitationId));
		invitatoryService.resendInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void removeInvitation(String projectId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(projectId, invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", projectId, invitationId));
		invitatoryService.removeInvitation(invitationId);
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
