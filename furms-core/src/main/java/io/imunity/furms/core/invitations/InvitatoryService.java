/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.UnsupportedUserException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyAppliedForMembershipException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.core.notification.NotificationService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.invitations.RemoveInvitationUserEvent;
import io.imunity.furms.domain.invitations.UpdateInvitationUserEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.utils.UTCTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;

@Service
public class InvitatoryService {
	private static final Logger LOG = LoggerFactory.getLogger(InvitatoryService.class);

	private final UsersDAO usersDAO;
	private final InvitationRepository invitationRepository;
	private final AuthzService authzService;
	private final NotificationService notificationService;
	private final ApplicationRepository applicationRepository;
	private final ApplicationEventPublisher publisher;
	private final Clock clock;
	private final int expirationTimeInSeconds;

	InvitatoryService(UsersDAO usersDAO, InvitationRepository invitationRepository, AuthzService authzService,
	                  NotificationService notificationService, ApplicationEventPublisher publisher, Clock clock,
	                  ApplicationRepository applicationRepository,
	                  @Value("${furms.invitations.expiration-time-in-seconds}") String expirationTime) {
		this.usersDAO = usersDAO;
		this.invitationRepository = invitationRepository;
		this.authzService = authzService;
		this.notificationService = notificationService;
		this.applicationRepository = applicationRepository;
		this.publisher = publisher;
		this.clock = clock;
		this.expirationTimeInSeconds = Integer.parseInt(expirationTime);
	}

	public Set<Invitation> getInvitations(Role role, UUID resourceId){
		return invitationRepository.findAllBy(role, resourceId);
	}

	public boolean checkAssociation(String resourceId, InvitationId invitationId) {
		return invitationRepository.findBy(invitationId)
			.filter(invitation -> invitation.resourceId.id.toString().equals(resourceId))
			.isPresent();
	}

	public void inviteUser(PersistentId userId, ResourceId resourceId, Role role, String resourceName) {
		FURMSUser user = usersDAO.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("Could not invite user due to wrong email address."));

		if(user.fenixUserId.isEmpty())
			throw new UnsupportedUserException("Only fenix users supported");
		if(invitationRepository.findBy(user.email, role, resourceId).isPresent())
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSupportRoleCheckExistingAlsoForAdminRole(resourceId, role, user.email))
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSiteAdminRoleCheckExistingAlsoForSupportRole(resourceId, role, user.email))
			throw new DuplicatedInvitationError("This invitation already exists");
		if(resourceId.type.equals(PROJECT) && applicationRepository.existsBy(resourceId.id.toString(), user.fenixUserId.get()))
			throw new UserAlreadyAppliedForMembershipException("User waiting for application approval");
		if(usersDAO.getUserAttributes(user.fenixUserId.get()).attributesByResource.getOrDefault(resourceId, Set.of()).contains(new UserAttribute(role)))
			throw new UserAlreadyHasRoleError("User already has this role");

		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.resourceName(resourceName)
			.userId(user.fenixUserId.get())
			.originator(authzService.getCurrentAuthNUser().email)
			.email(user.email)
			.utcExpiredAt(getExpiredAtTime())
			.build();

		invitationRepository.create(invitation);
		LOG.info("Inviting FENIX admin role to {}", userId);
		notificationService.notifyUserAboutNewRole(user.id.get(), invitation.role);
		publisher.publishEvent(new InviteUserEvent(user.fenixUserId.get(), user.email, resourceId));
	}

	private boolean isSiteAdminRoleCheckExistingAlsoForSupportRole(ResourceId resourceId, Role role, String email) {
		return role.equals(Role.SITE_ADMIN) && invitationRepository.findBy(email, Role.SITE_SUPPORT, resourceId).isPresent();
	}

	private boolean isSupportRoleCheckExistingAlsoForAdminRole(ResourceId resourceId, Role role, String email) {
		return role.equals(Role.SITE_SUPPORT) && invitationRepository.findBy(email, Role.SITE_ADMIN, resourceId).isPresent();
	}

	public void resendInvitation(InvitationId id) {
		invitationRepository.findBy(id).ifPresent(invitation -> {
			LocalDateTime expiredAt = getExpiredAtTime();
			invitationRepository.updateExpiredAt(invitation.id, expiredAt);
			Optional<FURMSUser> furmsUser = usersDAO.getAllUsers().stream()
				.filter(user -> user.email.equals(invitation.email))
				.findAny();
			if(invitation.code.isPresent() && furmsUser.isEmpty())
				usersDAO.resendInvitation(invitation, expiredAt.toInstant(ZoneOffset.UTC));
			else
				notificationService.notifyUserAboutNewRole(furmsUser.get().id.get(), invitation.role);

			publisher.publishEvent(new UpdateInvitationUserEvent(invitation.userId, invitation.email, invitation.id));
		});
	}

	public void updateInvitationRole(InvitationId id, Role role) {
		invitationRepository.findBy(id).ifPresent(invitation -> {
			LocalDateTime expiredAt = getExpiredAtTime();
			invitationRepository.updateExpiredAtAndRole(invitation.id, expiredAt, role);
			Optional<FURMSUser> furmsUser = usersDAO.getAllUsers().stream()
				.filter(user -> user.email.equals(invitation.email))
				.findAny();
			if(invitation.code.isPresent() && furmsUser.isEmpty())
				usersDAO.resendInvitation(invitation, expiredAt.toInstant(ZoneOffset.UTC), role);
			else
				notificationService.notifyUserAboutNewRole(furmsUser.get().id.get(), role);

			publisher.publishEvent(new UpdateInvitationUserEvent(invitation.userId, invitation.email, invitation.id));
		});
	}

	public void removeInvitation(InvitationId id) {
		invitationRepository.findBy(id).ifPresent(invitation -> {
			invitationRepository.deleteBy(id);
			if(invitation.code.isPresent())
				usersDAO.removeInvitation(invitation.code);
			publisher.publishEvent(new RemoveInvitationUserEvent(invitation.userId, invitation.email, invitation.id));
		});
	}

	public void inviteUser(String email, ResourceId resourceId, Role role, String resourceName) {
		Optional<FURMSUser> furmsUser = usersDAO.getAllUsers().stream()
			.filter(user -> user.email.equals(email))
			.findAny();
		if(furmsUser.isPresent()){
			inviteUser(furmsUser.get().id.get(), resourceId, role, resourceName);
			return;
		}

		if(!isEmailValid(email))
			throw new IllegalArgumentException("Email is not valid");
		if(invitationRepository.findBy(email, role, resourceId).isPresent())
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSupportRoleCheckExistingAlsoForAdminRole(resourceId, role, email))
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSiteAdminRoleCheckExistingAlsoForSupportRole(resourceId, role, email))
			throw new DuplicatedInvitationError("This invitation already exists");

		LocalDateTime expiredAt = getExpiredAtTime();
		InvitationCode invitationCode = usersDAO.inviteUser(resourceId, role, email, expiredAt.toInstant(ZoneOffset.UTC));

		try {
			invitationRepository.create(
				Invitation.builder()
					.resourceId(resourceId)
					.resourceName(resourceName)
					.role(role)
					.email(email)
					.originator(authzService.getCurrentAuthNUser().email)
					.code(invitationCode)
					.utcExpiredAt(expiredAt)
					.build()
			);
		} catch (Exception e) {
			usersDAO.removeInvitation(invitationCode);
			throw e;
		}
	}

	private LocalDateTime getExpiredAtTime() {
		return UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock).plusSeconds(expirationTimeInSeconds));
	}

	public boolean isEmailValid(String email) {
		try {
			new InternetAddress(email).validate();
		} catch (AddressException ex) {
			return false;
		}
		return true;
	}
}
