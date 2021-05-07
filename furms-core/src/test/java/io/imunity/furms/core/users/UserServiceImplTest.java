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

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.Sets;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperation;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.InviteUserEvent;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.spi.ssh_key_installation.SSHKeyOperationRepository;
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
	private SSHKeyOperationRepository sshKeyOperationRepository;

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
	void shouldGetUserSSHKeyWithSitesWithCompletedStatus() {
		// given
		FenixUserId fid = new FenixUserId("id");
		PersistentId pid = new PersistentId("id");
		when(usersDAO.getUserAttributes(fid))
				.thenReturn(new UserAttributes(Collections.emptySet(), Collections.emptyMap()));
		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(sshKeyRepository.findAllByOwnerId(pid)).thenReturn(
				Sets.newHashSet(SSHKey.builder().id("key").sites(Sets.newHashSet("s1", "s2")).build()));

		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("key", "s1")).thenReturn(SSHKeyOperationJob
				.builder().operation(SSHKeyOperation.ADD).status(SSHKeyOperationStatus.DONE).build());
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("key", "s2")).thenReturn(SSHKeyOperationJob
				.builder().operation(SSHKeyOperation.ADD).status(SSHKeyOperationStatus.ACK).build());

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.sshKeys).hasSize(1);
		assertThat(userRecord.sshKeys.iterator().next().sites).hasSameElementsAs(Sets.newHashSet("s1"));
	}

	@Test
	void shouldGetUserSSHKeysForAllSites() {
		// given
		FenixUserId fid = new FenixUserId("id");
		PersistentId pid = new PersistentId("id");
		when(usersDAO.getUserAttributes(fid))
				.thenReturn(new UserAttributes(Collections.emptySet(), Collections.emptyMap()));
		when(usersDAO.getPersistentId(fid)).thenReturn(pid);
		when(sshKeyRepository.findAllByOwnerId(pid)).thenReturn(
				Sets.newHashSet(SSHKey.builder().id("key").sites(Sets.newHashSet("s1", "s2")).build()));

		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("key", "s1")).thenReturn(SSHKeyOperationJob
				.builder().operation(SSHKeyOperation.ADD).status(SSHKeyOperationStatus.DONE).build());
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId("key", "s2")).thenReturn(SSHKeyOperationJob
				.builder().operation(SSHKeyOperation.ADD).status(SSHKeyOperationStatus.DONE).build());

		// when
		UserRecord userRecord = service.getUserRecord(new FenixUserId("id"));

		// then
		assertThat(userRecord.sshKeys).hasSize(1);
		assertThat(userRecord.sshKeys.iterator().next().sites).hasSameElementsAs(Sets.newHashSet("s1", "s2"));
	}
}