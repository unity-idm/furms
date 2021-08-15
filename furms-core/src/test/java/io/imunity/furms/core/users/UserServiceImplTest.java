/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;
import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.projects.ProjectMembershipOnSite;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UserAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.spi.users.UsersDAO;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@InjectMocks
	private UserServiceImpl service;

	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private MembershipResolver resolver;
	@Mock
	private UserAllocationsService userAllocationsService;

	@Test
	void shouldAllowToInviteUser() {
		// given
		PersistentId id = new PersistentId("userId");
		when(usersDAO.findById(eq(id)))
				.thenReturn(Optional.of(FURMSUser.builder().id(id).email("email").build()));

		// when
		service.inviteFenixAdmin(id);

		// then
		verify(usersDAO, times(1)).addFenixAdminRole(eq(id));
		verify(publisher, times(1))
				.publishEvent(new InviteUserEvent(id, new ResourceId((String) null, APP_LEVEL)));
	}

	@Test
	void shouldGetCompleteUserInformation() {
		// given
		final FenixUserId fid = new FenixUserId("id");
		final PersistentId pid = new PersistentId("id");
		final UUID policy1 = UUID.randomUUID();
		final UUID policy2 = UUID.randomUUID();
		final Map<ResourceId, Set<UserAttribute>> resourceAttributes = Map.of(
				new ResourceId(UUID.randomUUID().toString(), SITE),
				Set.of(new UserAttribute("site", "attr")));
		final Set<UserAttribute> rootAttributes = Set.of(new UserAttribute("root", "attr"));
		final UserAttributes userAttribute = new UserAttributes(rootAttributes, resourceAttributes);
		final Set<SiteUser> siteUser = Set.of(new SiteUser(
				"siteId",
				"siteOauthClientId",
				Set.of(new ProjectMembershipOnSite("localUserId", "projId")),
				new PolicyAcceptanceAtSite(new PolicyId(policy1), "siteId", 1,
						ACCEPTED, Instant.now()),
				Set.of(new PolicyAcceptanceAtSite(new PolicyId(policy2), "siteId", 2,
						ACCEPTED, Instant.now())),
				Set.of(new SiteSSHKeys("siteId", Set.of("sshKey1")))));

		when(usersDAO.getUserAttributes(fid)).thenReturn(userAttribute);
		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(usersDAO.getUserStatus(fid)).thenReturn(ENABLED);
		when(resolver.filterExposedAttribtues(rootAttributes)).thenReturn(rootAttributes);
		when(userAllocationsService.findUserSitesInstallations(pid)).thenReturn(siteUser);

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.userStatus).isEqualTo(ENABLED);
		assertThat(userRecord.attributes).containsAll(rootAttributes);
		assertThat(userRecord.siteInstallations).containsAll(siteUser);
	}
}