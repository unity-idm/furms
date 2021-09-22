/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.invitations.InviteeService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.invitations.InvitationAcceptedEvent;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.invitations.RemoveInvitationUserEvent;
import io.imunity.furms.domain.users.AddUserEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.spi.users.FenixUsersDAO;
import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.AUTHENTICATED;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

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
	private final ApplicationEventPublisher publisher;

	InviteeServiceImpl(InvitationRepository invitationRepository, AuthzService authzService, UsersDAO usersDAO,
	                   SiteGroupDAO siteGroupDAO, CommunityGroupsDAO communityGroupsDAO, ProjectGroupsDAO projectGroupsDAO,
	                   ProjectRepository projectRepository, ApplicationEventPublisher publisher, FenixUsersDAO fenixUsersDAO) {
		this.invitationRepository = invitationRepository;
		this.authzService = authzService;
		this.usersDAO = usersDAO;
		this.fenixUsersDAO = fenixUsersDAO;
		this.siteGroupDAO = siteGroupDAO;
		this.communityGroupsDAO = communityGroupsDAO;
		this.projectGroupsDAO = projectGroupsDAO;
		this.projectRepository = projectRepository;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void acceptBy(InvitationId id) {
		FURMSUser user = authzService.getCurrentAuthNUser();
		Invitation invitation = invitationRepository.findBy(id)
			.orElseThrow(() -> new IllegalArgumentException(String.format("Invitation id %s doesn't exist for user %s", id, user.fenixUserId)));
		switch (invitation.resourceId.type){
			case APP_LEVEL:
				fenixUsersDAO.addFenixAdminRole(user.id.get());
				break;
			case SITE:
				siteGroupDAO.addSiteUser(invitation.resourceId.id.toString(), user.id.get(), invitation.role);
				break;
			case COMMUNITY:
				communityGroupsDAO.addAdmin(invitation.resourceId.id.toString(), user.id.get());
				break;
			case PROJECT:
				String projectId = invitation.resourceId.id.toString();
				String communityId = projectRepository.findById(projectId).get().getCommunityId();
				projectGroupsDAO.addProjectUser(communityId, projectId, user.id.get(), invitation.role);
				break;
		}
		invitationRepository.deleteBy(invitation.id);
		publisher.publishEvent(new InvitationAcceptedEvent(user.fenixUserId.get(), invitation.resourceId));
		publisher.publishEvent(new AddUserEvent(user.id.get(), invitation.resourceId));
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public Set<Invitation> findAllByCurrentUser() {
		FURMSUser user = authzService.getCurrentAuthNUser();
		if(user.fenixUserId.isEmpty())
			return Set.of();
		Optional<FenixUserId> fenixUserId = user.fenixUserId;
		return invitationRepository.findAllBy(fenixUserId.get(), user.email);
	}

	@Override
	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL)
	public void removeBy(InvitationId id) {
		FURMSUser user = authzService.getCurrentAuthNUser();
		invitationRepository.findBy(id, user.fenixUserId.get()).ifPresent(invitation -> {
			invitationRepository.deleteBy(id);
			publisher.publishEvent(new RemoveInvitationUserEvent(invitation.userId, invitation.id, invitation.code));
		});
	}

	@Override
	@FurmsPublicAccess
	public void acceptInvitationByRegistration(String registrationId) {
		InvitationCode invitationCode = usersDAO.findByRegistrationId(registrationId);
		invitationRepository.findBy(invitationCode).ifPresent(invitation -> {
			invitationRepository.deleteBy(invitationCode);
		});
	}
}