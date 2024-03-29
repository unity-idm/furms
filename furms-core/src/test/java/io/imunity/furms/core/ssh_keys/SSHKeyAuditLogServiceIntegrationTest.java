/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLog;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.UPDATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
class SSHKeyAuditLogServiceIntegrationTest {

	@Autowired
	private SSHKeyRepository repository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private UsersDAO usersDAO;
	@Autowired
	private SSHKeyOperationRepository sshKeyOperationRepository;
	@Autowired
	private UserOperationRepository userOperationRepository;

	@Autowired
	private AuthzService authzService;
	@Autowired
	private AuditLogRepository auditLogRepository;

	@Autowired
	private SSHKeyService service;

	@BeforeEach
	void setUp() {
		TransactionSynchronizationManager.initSynchronization();
	}

	@AfterEach
	void clear() {
		TransactionSynchronizationManager.clear();
	}

	@Test
	void shouldDetectSSHKeyDeletion() {
		// given
		SSHKeyId id = new SSHKeyId(UUID.randomUUID());
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));

		when(repository.exists(id)).thenReturn(true);
		when(repository.findById(id)).thenReturn(Optional.of(SSHKey.builder().id(id).name("name")
			.ownerId(new PersistentId("ownerId")).sites(Set.of(s1)).build()));
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("ownerId"));
		when(usersDAO.findById(new PersistentId("ownerId"))).thenReturn(Optional
			.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(siteRepository.findById(s1)).thenReturn(Optional.of(Site.builder().id(s1).build()));
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId(id, s1))
			.thenReturn(SSHKeyOperationJob.builder().operation(ADD).status(SSHKeyOperationStatus.DONE).build());

		// when
		service.delete(id);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SSH_KEYS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.DELETE, argument.getValue().action);
	}

	@Test
	void shouldDetectSSHKeyUpdate() {
		// given
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SiteId s2 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKeyId sshKeyId = new SSHKeyId(UUID.randomUUID());
		SSHKey oldKey = getKey(sshKeyId,"name", Set.of(s1));
		SSHKey request = getKey(sshKeyId,"brandNewName", Set.of(s1, s2));
		SSHKey expectedKey = getKey(sshKeyId, request.name, Set.of(s1, s2));
		FenixUserId userId = new FenixUserId("id");

		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
			.of(FURMSUser.builder().email("email").fenixUserId(userId).build()));
		when(repository.exists(request.id)).thenReturn(true);
		when(repository.isNamePresentIgnoringRecord(request.name, request.id)).thenReturn(false);
		when(repository.update(expectedKey)).thenReturn(request.id);
		when(repository.findById(request.id)).thenReturn(Optional.of(oldKey));
		when(siteRepository.exists(s1)).thenReturn(true);
		when(siteRepository.exists(s2)).thenReturn(true);
		when(siteRepository.findById(s1)).thenReturn(Optional.of(Site.builder().id(s1).build()));
		when(siteRepository.findById(s2)).thenReturn(Optional.of(Site.builder().id(s2).build()));
		when(userOperationRepository.isUserAdded(s2, userId)).thenReturn(true);
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId(sshKeyId, s1))
			.thenReturn(SSHKeyOperationJob.builder().operation(UPDATE).status(SSHKeyOperationStatus.FAILED).build());
		when(sshKeyOperationRepository.findBySSHKeyIdAndSiteId(sshKeyId, s1))
			.thenReturn(SSHKeyOperationJob.builder().operation(UPDATE).status(SSHKeyOperationStatus.FAILED).build());
		when(userOperationRepository.isUserAdded(s2, userId)).thenReturn(true);

		// when
		service.update(request);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SSH_KEYS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.UPDATE, argument.getValue().action);
	}

	@Test
	void shouldDetectSSHKeyCreation() {
		SiteId s1 = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKeyId sshKeyId = new SSHKeyId(UUID.randomUUID());
		SSHKeyId xSshKeyId = new SSHKeyId(UUID.randomUUID());

		final SSHKey key = getKey(sshKeyId, "key", Set.of(s1));
		when(authzService.getCurrentUserId()).thenReturn(new PersistentId("id"));
		when(usersDAO.findById(new PersistentId("id"))).thenReturn(Optional
			.of(FURMSUser.builder().email("email").fenixUserId(new FenixUserId("id")).build()));
		when(repository.findById(xSshKeyId)).thenReturn(Optional.of(key));
		when(siteRepository.exists(s1)).thenReturn(true);
		when(siteRepository.findById(s1))
			.thenReturn(Optional.of(Site.builder().id(s1).sshKeyHistoryLength(10).build()));
		when(repository.create(key)).thenReturn(xSshKeyId);
		when(userOperationRepository.isUserAdded(s1, new FenixUserId("id"))).thenReturn(true);
		// when
		service.create(key);

		ArgumentCaptor<AuditLog> argument = ArgumentCaptor.forClass(AuditLog.class);
		Mockito.verify(auditLogRepository).create(argument.capture());
		assertEquals(Operation.SSH_KEYS_MANAGEMENT, argument.getValue().operationCategory);
		assertEquals(Action.CREATE, argument.getValue().action);
	}

	private SSHKey getKey(SSHKeyId sshKeyId, String name, Set<SiteId> sites) {
		return SSHKey.builder().id(sshKeyId).name(name).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
					+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
					+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
					+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
					+ "mb9xUkdFjZk9bL demo@demo.pl")
			.ownerId(new PersistentId("id")).sites(sites).build();
	}
}
