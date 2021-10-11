/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.applications;

import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.ApplicationNotExistingException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyInvitedException;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.applications.ProjectApplicationAcceptedEvent;
import io.imunity.furms.domain.applications.ProjectApplicationCreatedEvent;
import io.imunity.furms.domain.applications.ProjectApplicationWithUser;
import io.imunity.furms.domain.applications.ProjectApplicationRemovedEvent;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.AddUserEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.notifications.NotificationDAO;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_READ;
import static io.imunity.furms.domain.authz.roles.Capability.PROJECT_LIMITED_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
class ProjectApplicationsServiceImpl implements ProjectApplicationsService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationRepository applicationRepository;
	private final UsersDAO usersDAO;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final AuthzService authzService;
	private final NotificationDAO notificationDAO;
	private final InvitationRepository invitationRepository;
	private final ApplicationEventPublisher publisher;

	ProjectApplicationsServiceImpl(ApplicationRepository applicationRepository, UsersDAO usersDAO,
	                               ProjectGroupsDAO projectGroupsDAO, ProjectRepository projectRepository,
	                               AuthzService authzService, NotificationDAO notificationDAO,  InvitationRepository invitationRepository,
	                               ApplicationEventPublisher publisher) {
		this.applicationRepository = applicationRepository;
		this.usersDAO = usersDAO;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.authzService = authzService;
		this.notificationDAO = notificationDAO;
		this.invitationRepository = invitationRepository;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id="projectId")
	public List<FURMSUser> findAllApplyingUsers(String projectId) {
		Set<FenixUserId> usersIds = applicationRepository.findAllApplyingUsers(projectId);
		return usersDAO.getAllUsers().stream()
			.filter(usr -> usr.fenixUserId.isPresent())
			.filter(usr -> usersIds.contains(usr.fenixUserId.get()))
			.collect(toList());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public List<ProjectApplicationWithUser> findAllApplicationsUsersForCurrentProjectAdmins() {
		List<UUID> projectIds = authzService.getRoles().entrySet().stream()
			.filter(e -> e.getValue().contains(Role.PROJECT_ADMIN))
			.map(e -> e.getKey().id)
			.collect(toList());

		Map<FenixUserId, FURMSUser> collect = usersDAO.getAllUsers().stream()
			.filter(x -> x.fenixUserId.isPresent())
			.collect(toMap(x -> x.fenixUserId.get(), x -> x));

		return applicationRepository.findAllApplyingUsers(projectIds).stream()
			.map(x -> new ProjectApplicationWithUser(x.projectId, x.projectName, collect.get(x.userId)))
			.collect(Collectors.toList());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public Set<String> findAllAppliedProjectsIdsForCurrentUser() {
		FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(UserWithoutFenixIdValidationError::new);
		return applicationRepository.findAllAppliedProjectsIds(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public void createForCurrentUser(String projectId) {
		projectRepository.findById(projectId).ifPresent(project -> {
			FURMSUser currentUser = authzService.getCurrentAuthNUser();
			if(invitationRepository.findBy(currentUser.email, Role.PROJECT_USER, new ResourceId(projectId, PROJECT)).isPresent())
				throw new UserAlreadyInvitedException(String.format("User %s is invited for project %s", currentUser.email, projectId));
			FenixUserId fenixUserId = currentUser.fenixUserId
				.orElseThrow(UserWithoutFenixIdValidationError::new);
			applicationRepository.create(projectId, fenixUserId);
			projectGroupsDAO.getAllAdmins(project.getCommunityId(), projectId)
				.forEach(usr -> notificationDAO.notifyAdminAboutApplicationRequest(usr.id.get(), projectId, project.getName(), currentUser.email));
			publisher.publishEvent(new ProjectApplicationCreatedEvent(fenixUserId, projectId, new HashSet<>(projectGroupsDAO.getAllAdmins(project.getCommunityId(), projectId))));
			LOG.info("User {} application for project ID: {} was created", projectId, currentUser.fenixUserId.get());
		});
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = PROJECT)
	public void removeForCurrentUser(String projectId) {
		FenixUserId fenixUserId = authzService.getCurrentAuthNUser().fenixUserId
			.orElseThrow(UserWithoutFenixIdValidationError::new);

		if(applicationRepository.existsBy(projectId, fenixUserId)) {
			projectRepository.findById(projectId).ifPresent(project -> {
				applicationRepository.remove(projectId, fenixUserId);
				publisher.publishEvent(
					new ProjectApplicationRemovedEvent(fenixUserId, projectId, new HashSet<>(projectGroupsDAO.getAllAdmins(project.getCommunityId(), projectId)))
				);
				LOG.info("User {} application for project ID: {} was removed", projectId, fenixUserId);
			});
		} else
			throw new ApplicationNotExistingException("Application already doesn't exist");
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void accept(String projectId, FenixUserId fenixUserId) {
		if(applicationRepository.existsBy(projectId, fenixUserId)) {
			projectRepository.findById(projectId).ifPresent(project -> {
				String communityId = project.getCommunityId();
				PersistentId persistentId = usersDAO.getPersistentId(fenixUserId);
				projectGroupsDAO.addProjectUser(communityId, projectId, persistentId, Role.PROJECT_USER);
				applicationRepository.remove(projectId, fenixUserId);
				notificationDAO.notifyUserAboutApplicationAcceptance(persistentId, project.getName());
				publisher.publishEvent(
					new ProjectApplicationAcceptedEvent(fenixUserId, projectId, new HashSet<>(projectGroupsDAO.getAllAdmins(project.getCommunityId(), projectId)))
				);
				publisher.publishEvent(new AddUserEvent(persistentId, new ResourceId(projectId, PROJECT)));
				LOG.info("User {} application for project ID: {} was accepted", projectId, fenixUserId);
			});
		} else
			throw new ApplicationNotExistingException("Application already doesn't exist");
	}

	@Override
	@FurmsAuthorize(capability = PROJECT_LIMITED_WRITE, resourceType = PROJECT, id="projectId")
	public void remove(String projectId, FenixUserId fenixUserId) {
		if(applicationRepository.existsBy(projectId, fenixUserId)) {
			projectRepository.findById(projectId).ifPresent(project -> {
				applicationRepository.remove(projectId, fenixUserId);
				PersistentId persistentId = usersDAO.getPersistentId(fenixUserId);
				notificationDAO.notifyUserAboutApplicationRejection(persistentId, project.getName());
				publisher.publishEvent(
					new ProjectApplicationRemovedEvent(fenixUserId, projectId, new HashSet<>(projectGroupsDAO.getAllAdmins(project.getCommunityId(), projectId)))
				);
				LOG.info("User {} application for project ID: {} was rejected", projectId, fenixUserId);
			});
		} else
			throw new ApplicationNotExistingException("Application already doesn't exist");
	}
}
