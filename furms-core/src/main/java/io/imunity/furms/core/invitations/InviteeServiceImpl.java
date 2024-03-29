/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.invitations.InviteeService;
import io.imunity.furms.api.validation.exceptions.InvitationNotExistingException;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationAcceptedEvent;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.invitations.RemoveInvitationUserEvent;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.UserRoleGrantedByInvitationEvent;
import io.imunity.furms.domain.users.UserRoleGrantedByRegistrationEvent;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.users.FenixUsersDAO;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@Service
class InviteeServiceImpl implements InviteeService {

	private final InvitationRepository invitationRepository;
	private final AuthzService authzService;
	private final FenixUsersDAO fenixUsersDAO;
	private final UsersDAO usersDAO;
	private final SiteGroupDAO siteGroupDAO;
	private final CommunityGroupsDAO communityGroupsDAO;
	private final ProjectGroupsDAO projectGroupsDAO;
	private final ProjectRepository projectRepository;
	private final UserInvitationNotificationService userInvitationNotificationService;
	private final ApplicationEventPublisher publisher;

	InviteeServiceImpl(InvitationRepository invitationRepository, AuthzService authzService, UsersDAO usersDAO,
	                   SiteGroupDAO siteGroupDAO, CommunityGroupsDAO communityGroupsDAO, ProjectGroupsDAO projectGroupsDAO,
	                   ProjectRepository projectRepository, ApplicationEventPublisher publisher, FenixUsersDAO fenixUsersDAO,
	                   UserInvitationNotificationService userInvitationNotificationService) {
		this.invitationRepository = invitationRepository;
		this.authzService = authzService;
		this.usersDAO = usersDAO;
		this.fenixUsersDAO = fenixUsersDAO;
		this.siteGroupDAO = siteGroupDAO;
		this.communityGroupsDAO = communityGroupsDAO;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.userInvitationNotificationService = userInvitationNotificationService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public void acceptBy(InvitationId id) {
		FURMSUser user = authzService.getCurrentAuthNUser();
		Invitation invitation = invitationRepository.findBy(id)
			.orElseThrow(() -> new InvitationNotExistingException(String.format("Invitation id %s doesn't exist for user %s", id, user.fenixUserId)));
		acceptUserInvitation(user, invitation);
		invitationRepository.deleteBy(invitation.id);
		notifyOriginatorAndSameHierarchyAdmins(invitation, usr -> userInvitationNotificationService.notifyAdminAboutRoleAcceptance(usr.id.get(), invitation.role, invitation.email));
		publisher.publishEvent(new InvitationAcceptedEvent(user.fenixUserId.orElse(FenixUserId.empty()), user.email,
			invitation.resourceId));
		publisher.publishEvent(new UserRoleGrantedByInvitationEvent(invitation.originator, user.id.get(), invitation.resourceId, invitation.resourceName, invitation.role));
	}

	void acceptUserInvitation(FURMSUser user, Invitation invitation) {
		switch (invitation.resourceId.type){
			case APP_LEVEL:
				fenixUsersDAO.addFenixAdminRole(user.id.get());
				break;
			case SITE:
				siteGroupDAO.addSiteUser(invitation.resourceId.asSiteId(), user.id.get(), invitation.role);
				break;
			case COMMUNITY:
				communityGroupsDAO.addAdmin(invitation.resourceId.asCommunityId(), user.id.get());
				break;
			case PROJECT:
				ProjectId projectId = invitation.resourceId.asProjectId();
				CommunityId communityId = projectRepository.findById(projectId).get().getCommunityId();
				projectGroupsDAO.addProjectUser(communityId, projectId, user.id.get(), invitation.role);
				break;
		}
	}

	private void notifyOriginatorAndSameHierarchyAdmins(Invitation invitation, Consumer<FURMSUser> notifier){
		List<FURMSUser> adminsToNotify;
		switch (invitation.resourceId.type){
			case APP_LEVEL:
				adminsToNotify = fenixUsersDAO.getAdminUsers();
				break;
			case SITE:
				SiteId siteId = invitation.resourceId.asSiteId();
				adminsToNotify = siteGroupDAO.getSiteUsers(siteId, Set.of(Role.SITE_ADMIN));
				break;
			case COMMUNITY:
				CommunityId communityId = invitation.resourceId.asCommunityId();
				adminsToNotify = communityGroupsDAO.getAllAdmins(communityId);
				break;
			case PROJECT:
				ProjectId projectId = invitation.resourceId.asProjectId();
				CommunityId comId = projectRepository.findById(projectId).get().getCommunityId();
				adminsToNotify = projectGroupsDAO.getAllAdmins(comId, projectId);
				break;
			default:
				adminsToNotify = List.of();
		}
		usersDAO.getAllUsers().stream()
			.filter(usr -> usr.email.equals(invitation.originator))
			.collect(collectingAndThen(toSet(), furmsUsers -> {
				furmsUsers.addAll(adminsToNotify);
				return furmsUsers.stream();
			}))
			.filter(usr -> !usr.email.equals(invitation.email))
			.forEach(notifier);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public Set<Invitation> findAllByCurrentUser() {
		FURMSUser user = authzService.getCurrentAuthNUser();
		Set<Invitation> invitationsRelatedWithEmail = invitationRepository.findAllBy(user.email);
		if(user.fenixUserId.isEmpty())
			return invitationsRelatedWithEmail;
		Optional<FenixUserId> fenixUserId = user.fenixUserId;
		return Stream.concat(invitationsRelatedWithEmail.stream(), invitationRepository.findAllBy(fenixUserId.get()).stream())
			.collect(Collectors.toSet());
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED)
	public void removeBy(InvitationId id) {
		FURMSUser user = authzService.getCurrentAuthNUser();
		invitationRepository.findBy(id, user.email).ifPresent(invitation -> {
			invitationRepository.deleteBy(id);
			notifyOriginatorAndSameHierarchyAdmins(invitation, usr -> userInvitationNotificationService.notifyAdminAboutRoleRejection(usr.id.get(), invitation.role, invitation.email));
			publisher.publishEvent(new RemoveInvitationUserEvent(invitation.userId, invitation.email, invitation.id));
		});
	}

	@Override
	@FurmsPublicAccess
	public void acceptInvitationByRegistration(String registrationId) {
		InvitationCode invitationCode = usersDAO.findByRegistrationId(registrationId);
		invitationRepository.findBy(invitationCode).ifPresent(invitation -> {
			invitationRepository.deleteBy(invitationCode);
			notifyOriginatorAndSameHierarchyAdmins(invitation, usr -> userInvitationNotificationService.notifyAdminAboutRoleAcceptance(usr.id.get(), invitation.role, invitation.email));
			publisher.publishEvent(new UserRoleGrantedByRegistrationEvent(invitation.originator, invitation.resourceId, invitation.resourceName, invitation.role, invitation.email));
		});
	}
}
