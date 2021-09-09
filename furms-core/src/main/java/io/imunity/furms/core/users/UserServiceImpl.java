/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.domain.users.UnknownUserException;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.utils.UTCTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.imunity.furms.domain.authz.roles.Capability.FENIX_ADMINS_MANAGEMENT;
import static io.imunity.furms.domain.authz.roles.Capability.READ_ALL_USERS;
import static io.imunity.furms.domain.authz.roles.Capability.USERS_MAINTENANCE;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

@Service
class UserServiceImpl implements UserService {

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
	private static final int EXPIRATION_TIME_IN_DAYS = 7;

	private final UsersDAO usersDAO;
	private final UserAllocationsService userAllocationsService;
	private final MembershipResolver membershipResolver;
	private final InvitationRepository invitationRepository;
	private final AuthzService authzService;
	private final ApplicationEventPublisher publisher;

	public UserServiceImpl(UsersDAO usersDAO,
	                       MembershipResolver membershipResolver,
	                       ApplicationEventPublisher publisher,
	                       UserAllocationsService userAllocationsService,
	                       AuthzService authzService,
	                       InvitationRepository invitationRepository) {
		this.usersDAO = usersDAO;
		this.membershipResolver = membershipResolver;
		this.publisher = publisher;
		this.userAllocationsService = userAllocationsService;
		this.invitationRepository = invitationRepository;
		this.authzService = authzService;
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public List<FURMSUser> getAllUsers(){
		return usersDAO.getAllUsers();
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public List<FURMSUser> getFenixAdmins(){
		return usersDAO.getAdminUsers();
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public Set<Invitation> getFenixAdminsInvitations(){
		return invitationRepository.findAllBy(Role.FENIX_ADMIN, null);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void inviteFenixAdmin(PersistentId userId) {
		FURMSUser user = usersDAO.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Could not invite user due to wrong email address."));
		invitationRepository.create(
			Invitation.builder()
				.resourceId(new ResourceId((UUID) null, APP_LEVEL))
				.role(Role.FENIX_ADMIN)
				.userId(user.fenixUserId.get())
				.originator(authzService.getCurrentAuthNUser().email)
				.email(user.email)
				.utcExpiredAt(UTCTimeUtils.convertToUTCTime(ZonedDateTime.now().plusDays(EXPIRATION_TIME_IN_DAYS)))
				.build()
		);
		LOG.info("Inviting FENIX admin role to {}", userId);
		publisher.publishEvent(new InviteUserEvent(userId, new ResourceId((String) null, APP_LEVEL)));
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void resendFenixAdminInvitation(InvitationCode code) {
		usersDAO.resendFenixAdminInvitation(code);
	}

	@Override
	@Transactional
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminInvitation(InvitationCode code) {
		invitationRepository.deleteBy(code);
		usersDAO.removeFenixAdminInvitation(code);
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void inviteFenixAdmin(String email) {
		LocalDateTime expiredAt = UTCTimeUtils.convertToUTCTime(ZonedDateTime.now().plusDays(EXPIRATION_TIME_IN_DAYS));
		InvitationCode invitationCode = usersDAO.inviteFenixAdmin(email, expiredAt.toInstant(ZoneOffset.UTC));
		try {
			invitationRepository.create(
				Invitation.builder()
					.resourceId(new ResourceId((UUID) null, APP_LEVEL))
					.role(Role.FENIX_ADMIN)
					.email(email)
					.originator(authzService.getCurrentAuthNUser().email)
					.code(invitationCode)
					.utcExpiredAt(expiredAt)
					.build()
			);
		} catch (Exception e){
			usersDAO.removeFenixAdminInvitation(invitationCode);
			throw e;
		}
	}

	@Override
	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public void removeFenixAdminRole(PersistentId userId){
		LOG.info("Removing FENIX admin role from {}", userId);
		usersDAO.removeFenixAdminRole(userId);
		publisher.publishEvent(new RemoveUserRoleEvent(userId, new ResourceId((String) null, APP_LEVEL)));
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public void setUserStatus(FenixUserId fenixUserId, UserStatus status) {
		checkNotNull(status);
		checkNotNull(fenixUserId);
		LOG.info("Setting {} status to {}", fenixUserId, status);
		try {
			usersDAO.setUserStatus(fenixUserId, status);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserStatus getUserStatus(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			return usersDAO.getUserStatus(fenixUserId);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
	
	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<FURMSUser> findById(PersistentId userId) {
		checkNotNull(userId);
		checkNotNull(userId.id);
		return usersDAO.findById(userId);
	}

	@Override
	@FurmsAuthorize(capability = READ_ALL_USERS, resourceType = APP_LEVEL)
	public Optional<FURMSUser> findByFenixUserId(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		checkNotNull(fenixUserId.id);
		return usersDAO.findById(fenixUserId);
	}

	@Override
	@FurmsAuthorize(capability = USERS_MAINTENANCE, resourceType = APP_LEVEL)
	public UserRecord getUserRecord(FenixUserId fenixUserId) {
		checkNotNull(fenixUserId);
		try {
			final PersistentId userId = usersDAO.getPersistentId(fenixUserId);

			final UserAttributes userAttributes = usersDAO.getUserAttributes(fenixUserId);
			final UserStatus userStatus = usersDAO.getUserStatus(fenixUserId);
			final Set<UserAttribute> rootAttributes = membershipResolver
					.filterExposedAttribtues(userAttributes.rootAttributes);
			final Set<SiteUser> siteUsers = userAllocationsService.findUserSitesInstallations(userId);

			return new UserRecord(userStatus, rootAttributes, userAttributes.attributesByResource, siteUsers);
		} catch (UnityFailureException e) {
			LOG.info("Failed to resolve user", e);
			throw new UnknownUserException(fenixUserId);
		}
	}
}





