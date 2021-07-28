/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users;

import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserAllocationsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.Sets;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
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
	private SSHKeyRepository sshKeyRepository;
	@Mock
	private SiteService siteService;
	@Mock
	private InstalledSSHKeyRepository installedSSHKeyRepository;
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
	void shouldGetSiteSSHKeysWithSitesWithSuccessInstalledStatus() {
		// given
		FenixUserId fid = new FenixUserId("id");
		PersistentId pid = new PersistentId("id");
		when(usersDAO.getUserAttributes(fid))
				.thenReturn(new UserAttributes(Collections.emptySet(), Collections.emptyMap()));
		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(sshKeyRepository.findAllByOwnerId(pid)).thenReturn(Sets.newHashSet(
				SSHKey.builder().id("key").sites(Sets.newHashSet("s1", "s2")).value("v1").build()));
		when(siteService.findAll())
				.thenReturn(new HashSet<>(Arrays.asList(Site.builder().id("s1").name("site1").build(),
						Site.builder().id("s2").name("site2").build())));

		when(installedSSHKeyRepository.findBySSHKeyId("key"))
				.thenReturn(Arrays.asList(InstalledSSHKey.builder().siteId("s1").value("v1").build()));

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.sshKeys).hasSize(1);
		SiteSSHKeys siteSSHKeys = userRecord.sshKeys.iterator().next();

		assertThat(siteSSHKeys.sshKeys).hasSameElementsAs(Sets.newHashSet("v1"));
		assertThat(siteSSHKeys.siteId).isEqualTo("s1");
		assertThat(siteSSHKeys.siteName).isEqualTo("site1");

	}

	@Test
	void shouldGetUserSSHKeysForAllSites() {
		// given
		FenixUserId fid = new FenixUserId("id");
		PersistentId pid = new PersistentId("id");

		when(siteService.findAll())
				.thenReturn(new HashSet<>(Arrays.asList(Site.builder().id("s1").name("site1").build(),
						Site.builder().id("s2").name("site2").build())));
		when(usersDAO.getUserAttributes(fid))
				.thenReturn(new UserAttributes(Collections.emptySet(), Collections.emptyMap()));
		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(sshKeyRepository.findAllByOwnerId(pid)).thenReturn(Sets.newHashSet(
				SSHKey.builder().id("key").sites(Sets.newHashSet("s1", "s2")).value("v1").build()));

		when(installedSSHKeyRepository.findBySSHKeyId("key"))
				.thenReturn(Arrays.asList(InstalledSSHKey.builder().siteId("s1").value("v1").build(),
						InstalledSSHKey.builder().siteId("s2").value("v1").build()));

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.sshKeys).hasSize(2);
		SiteSSHKeys site1SSHKeys = userRecord.sshKeys.stream().filter(s -> s.siteId.equals("s1")).findAny()
				.get();
		SiteSSHKeys site2SSHKeys = userRecord.sshKeys.stream().filter(s -> s.siteId.equals("s2")).findAny()
				.get();

		assertThat(site1SSHKeys.sshKeys).hasSameElementsAs(Sets.newHashSet("v1"));
		assertThat(site1SSHKeys.siteId).isEqualTo("s1");
		assertThat(site1SSHKeys.siteName).isEqualTo("site1");

		assertThat(site2SSHKeys.sshKeys).hasSameElementsAs(Sets.newHashSet("v1"));
		assertThat(site2SSHKeys.siteId).isEqualTo("s2");
		assertThat(site2SSHKeys.siteName).isEqualTo("site2");
	}
}