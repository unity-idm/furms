/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.projects;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.core.project_installation.ProjectInstallationService;
import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.authz.roles.Capability;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.domain.projects.ProjectCreatedEvent;
import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.projects.ProjectRemovedEvent;
import io.imunity.furms.domain.projects.ProjectUpdatedEvent;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserProjectMembershipRevokedEvent;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_READ;
import static io.imunity.furms.domain.authz.roles.Capability.COMMUNITY_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LEAVE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.COMMUNITY;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
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
	private final ProjectInstallationsService projectInstallationsService;
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
	                          ProjectInstallationsService projectInstallationsService,
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
		this.projectInstallationsService = projectInstallationsService;
		this.capabilityCollector = capabilityCollector;
		this.invitatoryService = invitatoryService;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public boolean existsById(ProjectId id) {
		return projectRepository.exists(id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "ids", idCollections = true)
	public Set<Project> findAll(Set<ProjectId> ids) {
		return projectRepository.findAll(ids);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id.id")
	public Optional<Project> findById(ProjectId id) {
		return projectRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public Set<Project> findAllByCommunityId(CommunityId communityId) {
		return projectRepository.findAllByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public Set<Project> findAllNotExpiredByCommunityId(CommunityId communityId) {
		return projectRepository.findAllNotExpiredByCommunityId(communityId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT)
	public Set<Project> findAll() {
		return projectRepository.findAll();
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<Project> findAllByCurrentUserId() {
		final FURMSUser currentUser = authzService.getCurrentAuthNUser();
		return projectRepository.findAll().stream()
				.filter(project -> isProjectAdmin(project, currentUser.roles))
				.collect(toSet());
	}

	private boolean isProjectAdmin(Project project, Map<ResourceId, Set<Role>> roles) {
		final Set<Capability> capabilities = Set.of(PROJECT_READ, PROJECT_WRITE);
		return capabilityCollector.getCapabilities(roles, new ResourceId(project.getId().id, PROJECT))
				.stream().anyMatch(capabilities::contains);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public boolean isProjectInTerminalState(ProjectId projectId) {
		return projectInstallationService.isProjectInTerminalState(projectId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public boolean isProjectInTerminalState(CommunityId communityId, ProjectId projectId) {
		return projectInstallationService.isProjectInTerminalState(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id.id")
	public boolean isProjectExpired(ProjectId id) {
		final Optional<Project> project = findById(id);
		return project.isEmpty() || project.get().isExpired();
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "project.communityId.id")
	public ProjectId create(Project project) {
		validator.validateCreate(project);
		ProjectId id = projectRepository.create(project);
		projectGroupsDAO.create(new ProjectGroup(id, project.getName(), project.getCommunityId()));
		Project createdProject = projectRepository.findById(id).get();
		addAdmin(project.getCommunityId(), id, project.getLeaderId());
		publisher.publishEvent(new ProjectCreatedEvent(createdProject));
		LOG.info("Project with given ID: {} was created: {}", id, project);
		return id;
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_WRITE, resourceType = PROJECT, id = "project.id.id")
	public void update(Project project) {
		validator.validateUpdate(project);
		Project oldProject = projectRepository.findById(project.getId()).get();
		projectRepository.update(project);
		projectGroupsDAO.update(new ProjectGroup(project.getId(), project.getName(), project.getCommunityId()));
		addAdmin(project.getCommunityId(), project.getId(), project.getLeaderId());
		updateInAgent(project);
		publisher.publishEvent(new ProjectUpdatedEvent(oldProject, project));
		LOG.info("Project was updated {}", project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "attributes.id.id")
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
		publisher.publishEvent(new ProjectUpdatedEvent(project, updatedProject));
		LOG.info("Project was updated {}", attributes);
	}

	private void updateInAgent(Project project) {
		projectInstallationService.update(project);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = COMMUNITY_WRITE, resourceType = COMMUNITY, id = "communityId.id")
	public void delete(ProjectId projectId, CommunityId communityId) {
		validator.validateDelete(projectId);
		List<FURMSUser> allProjectUsers = projectGroupsDAO.getAllUsers(communityId, projectId);
		removeFromAgent(projectId);
		Project project = projectRepository.findById(projectId).get();
		projectRepository.delete(projectId);
		projectGroupsDAO.delete(communityId, projectId);
		publisher.publishEvent(new ProjectRemovedEvent(allProjectUsers, project));
		LOG.info("Project with given ID: {} was deleted", projectId);
	}

	private void removeFromAgent(ProjectId projectId) {
		projectInstallationService.remove(projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public List<FURMSUser> findAllAdmins(CommunityId communityId, ProjectId projectId){
		return projectGroupsDAO.getAllAdmins(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "communityId.id")
	public CommunityAdminsAndProjectAdmins findAllCommunityAndProjectAdmins(CommunityId communityId, ProjectId projectId) {
		return projectGroupsDAO.getAllCommunityAndProjectAdmins(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public boolean isAdmin(ProjectId projectId){
		return authzService.isResourceMember(projectId.id.toString(), PROJECT_ADMIN);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public boolean hasAdminRights(ProjectId projectId) {
		final Optional<Project> project = projectRepository.findById(projectId);
		return project.isPresent()
				&& (authzService.isResourceMember(project.get().getId().id.toString(), PROJECT_ADMIN)
					|| authzService.isResourceMember(project.get().getCommunityId().id.toString(), COMMUNITY_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public void addAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId){
		projectGroupsDAO.addProjectUser(communityId, projectId, userId, PROJECT_ADMIN);
		String projectName = projectRepository.findById(projectId).get().getName();
		publisher.publishEvent(new UserRoleGrantedEvent(userId, new ResourceId(projectId.id, PROJECT), projectName,
			PROJECT_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public Set<Invitation> findAllAdminsInvitations(ProjectId projectId) {
		return invitatoryService.getInvitations(PROJECT_ADMIN, projectId.id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public Set<Invitation> findAllUsersInvitations(ProjectId projectId) {
		return invitatoryService.getInvitations(PROJECT_USER, projectId.id);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public void inviteAdmin(ProjectId projectId, PersistentId id){
		projectRepository.findById(projectId).ifPresent(project ->
			invitatoryService.inviteUser(id, new ResourceId(projectId.id, PROJECT), PROJECT_ADMIN, project.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public void inviteAdmin(ProjectId projectId, String email){
		projectRepository.findById(projectId).ifPresent(project ->
			invitatoryService.inviteUser(email, new ResourceId(projectId.id, PROJECT), PROJECT_ADMIN, project.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_ADMINS_MANAGEMENT, resourceType = PROJECT, id = "projectId.id")
	public void removeAdmin(CommunityId communityId, ProjectId projectId, PersistentId userId){
		projectGroupsDAO.removeAdmin(communityId, projectId, userId);
		String projectName = projectRepository.findById(projectId).get().getName();
		publisher.publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(projectId.id, PROJECT), projectName,
			PROJECT_ADMIN));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public List<FURMSUser> findAllUsers(CommunityId communityId, ProjectId projectId){
		return projectGroupsDAO.getAllUsers(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public List<FURMSUser> findAllProjectAdminsAndUsers(CommunityId communityId, ProjectId projectId) {
		return projectGroupsDAO.getAllProjectAdminsAndUsers(communityId, projectId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "projectId.id")
	public List<FURMSUser> findAllUsers(ProjectId projectId) {
		return projectRepository.findById(projectId)
			.map(project -> projectGroupsDAO.getAllUsers(project.getCommunityId(), projectId))
			.orElseGet(List::of);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Optional<FURMSUser> findProjectLeaderInfoAsInstalledUser(ProjectId projectId) {
		return projectInstallationsService.findAllSiteInstalledProjectsOfCurrentUser().stream()
				.filter(siteInstalledProject -> siteInstalledProject.project.getId().equals(projectId))
				.findFirst()
				.map(siteInstalledProject -> usersDAO.findById(siteInstalledProject.project.getLeaderId()))
				.map(Optional::get);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public boolean isUser(ProjectId projectId) {
		return authzService.isResourceMember(projectId.id.toString(), PROJECT_USER);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<ProjectId> getUsersProjectIds() {
		return authzService.getRoles().entrySet().stream()
			.filter(entry -> entry.getValue().stream().anyMatch(role -> role.equals(PROJECT_USER)))
			.map(entry -> entry.getKey().id)
			.filter(Objects::nonNull)
			.map(ProjectId::new)
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId.id")
	public void addUser(CommunityId communityId, ProjectId projectId, PersistentId userId){
		projectGroupsDAO.addProjectUser(communityId, projectId, userId, PROJECT_USER);
		String projectName = projectRepository.findById(projectId).get().getName();
		publisher.publishEvent(new UserRoleGrantedEvent(userId, new ResourceId(projectId.id, PROJECT), projectName,
			PROJECT_USER));
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId.id")
	public void inviteUser(ProjectId projectId, PersistentId userId) {
		projectRepository.findById(projectId).ifPresent(project ->
			invitatoryService.inviteUser(userId, new ResourceId(projectId.id, PROJECT), PROJECT_USER, project.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId.id")
	public void inviteUser(ProjectId projectId, String email) {
		projectRepository.findById(projectId).ifPresent(project ->
			invitatoryService.inviteUser(email, new ResourceId(projectId.id, PROJECT), PROJECT_USER, project.getName())
		);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId.id")
	public void resendInvitation(ProjectId projectId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(projectId.id.toString(), invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", projectId, invitationId));
		invitatoryService.resendInvitation(invitationId);
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId.id")
	public void removeInvitation(ProjectId projectId, InvitationId invitationId) {
		if(!invitatoryService.checkAssociation(projectId.id.toString(), invitationId))
			throw new IllegalArgumentException(String.format("Invitation %s is not associate with this resource %s", projectId, invitationId));
		invitatoryService.removeInvitation(invitationId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id = "projectId.id")
	public void removeUser(CommunityId communityId, ProjectId projectId, PersistentId userId){
		removeUserFromProject(communityId, projectId, userId);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LEAVE, resourceType = PROJECT, id = "projectId.id")
	public void resignFromMembership(CommunityId communityId, ProjectId projectId) {
		final PersistentId userId = authzService.getCurrentUserId();
		removeUserFromProject(communityId, projectId, userId);
	}

	private void removeUserFromProject(CommunityId communityId, ProjectId projectId, PersistentId userId) {
		userOperationService.createUserRemovals(projectId, userId);
		projectGroupsDAO.removeUser(communityId, projectId, userId);
		String projectName = projectRepository.findById(projectId).get().getName();
		publisher.publishEvent(new UserProjectMembershipRevokedEvent(userId, new ResourceId(projectId.id, PROJECT),
			projectName));
	}
}
