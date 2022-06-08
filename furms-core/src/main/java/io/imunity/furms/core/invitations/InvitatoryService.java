/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.invitations;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.InvalidEmailException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyAppliedForMembershipException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.invitations.Invitation;
import io.imunity.furms.domain.invitations.InvitationCode;
import io.imunity.furms.domain.invitations.InvitationId;
import io.imunity.furms.domain.invitations.InviteUserEvent;
import io.imunity.furms.domain.invitations.RemoveInvitationUserEvent;
import io.imunity.furms.domain.invitations.UpdateInvitationUserEvent;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserRoleGrantedEvent;
import io.imunity.furms.spi.applications.ApplicationRepository;
import io.imunity.furms.spi.invitations.InvitationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.utils.UTCTimeUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
	private final UserInvitationNotificationService userInvitationNotificationService;
	private final ApplicationRepository applicationRepository;
	private final InviteeServiceImpl inviteeService;
	private final ApplicationEventPublisher publisher;
	private final Clock clock;
	private final int expirationTimeInSeconds;

	InvitatoryService(UsersDAO usersDAO, InvitationRepository invitationRepository, AuthzService authzService,
	                  UserInvitationNotificationService userInvitationNotificationService, ApplicationEventPublisher publisher, Clock clock,
	                  ApplicationRepository applicationRepository, InviteeServiceImpl inviteeService,
	                  @Value("${furms.invitations.expiration-time-in-seconds}") String expirationTime) {
		this.usersDAO = usersDAO;
		this.invitationRepository = invitationRepository;
		this.authzService = authzService;
		this.userInvitationNotificationService = userInvitationNotificationService;
		this.applicationRepository = applicationRepository;
		this.inviteeService = inviteeService;
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

		if(invitationRepository.findBy(user.email, role, resourceId).isPresent())
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSupportRoleCheckExistingAlsoForAdminRole(resourceId, role, user.email))
			throw new DuplicatedInvitationError("This invitation already exists");
		if(isSiteAdminRoleCheckExistingAlsoForSupportRole(resourceId, role, user.email))
			throw new DuplicatedInvitationError("This invitation already exists");
		if(resourceId.type.equals(PROJECT) && applicationRepository.existsBy(new ProjectId(resourceId.id),
			user.fenixUserId.get()))
			throw new UserAlreadyAppliedForMembershipException("User waiting for application approval");
		if(containsRole(usersDAO.getUserAttributes(userId).attributesByResource.getOrDefault(resourceId, Set.of()), role))
			throw new UserAlreadyHasRoleError("User already has this role");

		String originator = authzService.getCurrentAuthNUser().email;

		Invitation invitation = Invitation.builder()
			.resourceId(resourceId)
			.role(role)
			.resourceName(resourceName)
			.userId(user.fenixUserId.orElse(FenixUserId.empty()))
			.originator(originator)
			.email(user.email)
			.utcExpiredAt(getExpiredAtTime())
			.build();

		if(originator.equals(user.email)){
			autoAcceptInvitation(userId, user, invitation);
			return;
		}

		invitationRepository.create(invitation);
		LOG.info("Inviting FENIX admin role to {}", userId);
		userInvitationNotificationService.notifyUserAboutNewRole(userId, invitation.role);
		publisher.publishEvent(new InviteUserEvent(user.fenixUserId.orElse(FenixUserId.empty()), user.email, resourceId));
	}

	private void autoAcceptInvitation(PersistentId userId, FURMSUser user, Invitation invitation) {
		inviteeService.acceptUserInvitation(user, invitation);
		publisher.publishEvent(new UserRoleGrantedEvent(userId,  invitation.resourceId, invitation.resourceName,
			invitation.role));
	}

	private boolean containsRole(Set<UserAttribute> userAttributes, Role role) {
		return userAttributes.stream()
			.filter(userAttribute -> userAttribute.name.equals(role.unityRoleAttribute))
			.anyMatch(userAttribute -> userAttribute.values.contains(role.unityRoleValue));
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
				userInvitationNotificationService.notifyUserAboutNewRole(furmsUser.get().id.get(), invitation.role);

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
				userInvitationNotificationService.notifyUserAboutNewRole(furmsUser.get().id.get(), role);

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
			throw new InvalidEmailException("Email is not valid");
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

	private boolean isEmailValid(String email) {
		return EmailValidator.getInstance().isValid(email);
	}
}
