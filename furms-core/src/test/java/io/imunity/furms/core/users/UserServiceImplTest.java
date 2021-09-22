/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.domain.policy_documents.PolicyAcceptanceAtSite;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.projects.ProjectMembershipOnSite;
import io.imunity.furms.domain.sites.SiteUser;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.policy_documents.PolicyAcceptanceStatus.ACCEPTED;
import static io.imunity.furms.domain.users.UserStatus.ENABLED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@InjectMocks
	private UserServiceImpl service;

	@Mock
	private UsersDAO usersDAO;
	@Mock
	private UserAllocationsService userAllocationsService;

	@Test
	void shouldGetCompleteUserInformation() {
		// given
		final FenixUserId fid = new FenixUserId("id");
		final PersistentId pid = new PersistentId("id");
		final UUID policy1 = UUID.randomUUID();
		final UUID policy2 = UUID.randomUUID();
		final FURMSUser furmsUser = FURMSUser.builder()
				.id(pid)
				.fenixUserId(fid)
				.firstName("firstName")
				.lastName("lastName")
				.email("email@domain.com")
				.status(ENABLED)
				.build();
		final Set<SiteUser> siteUser = Set.of(new SiteUser(
				"siteId",
				"siteOauthClientId",
				Set.of(new ProjectMembershipOnSite("localUserId", "projId")),
				new PolicyAcceptanceAtSite(new PolicyId(policy1), "siteId", 1,
						ACCEPTED, Instant.now()),
				Set.of(new PolicyAcceptanceAtSite(new PolicyId(policy2), "siteId", 2,
						ACCEPTED, Instant.now())),
				Set.of(new SiteSSHKeys("siteId", Set.of("sshKey1")))));

		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(usersDAO.findById(fid)).thenReturn(Optional.of(furmsUser));
		when(userAllocationsService.findUserSitesInstallations(pid)).thenReturn(siteUser);

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.user).isEqualTo(furmsUser);
		assertThat(userRecord.siteInstallations).containsAll(siteUser);
	}
}