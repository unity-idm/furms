/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.users.FenixUserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.users.FenixUsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.FENIX_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

@Service
class FenixUserServiceImpl implements FenixUserService {
	private static final Logger LOG = LoggerFactory.getLogger(FenixUserServiceImpl.class);
	public static final String RESOURCE_NAME = "system";

	private final FenixUsersDAO usersDAO;
	private final InvitatoryService invitatoryService;
	private final ApplicationEventPublisher publisher;

	FenixUserServiceImpl(FenixUsersDAO usersDAO, InvitatoryService invitatoryService, ApplicationEventPublisher publisher) {
		this.usersDAO = usersDAO;
		this.invitatoryService = invitatoryService;
		this.publisher = publisher;
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public List<FURMSUser> getFenixAdmins(){
		return usersDAO.getAdminUsers();
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public Set<Invitation> getFenixAdminsInvitations(){
		return invitatoryService.getInvitations(Role.FENIX_ADMIN, null);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void inviteFenixAdmin(PersistentId userId) {
		invitatoryService.inviteUser(userId, new ResourceId(null, APP_LEVEL), Role.FENIX_ADMIN, RESOURCE_NAME);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void resendFenixAdminInvitation(InvitationId id) {
		invitatoryService.resendInvitation(id);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void removeFenixAdminInvitation(InvitationId id) {
		invitatoryService.removeInvitation(id);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void inviteFenixAdmin(String email) {
		invitatoryService.inviteUser(email, new ResourceId(null, APP_LEVEL), Role.FENIX_ADMIN, RESOURCE_NAME);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void removeFenixAdminRole(PersistentId userId){
		LOG.info("Removing FENIX admin role from {}", userId);
		usersDAO.removeFenixAdminRole(userId);
		publisher.publishEvent(new UserRoleRevokedEvent(userId, new ResourceId(null, APP_LEVEL), RESOURCE_NAME, Role.FENIX_ADMIN));
	}
}
