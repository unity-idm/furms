/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.google.common.collect.Sets;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.IllegalStateTransitionException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperation;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationError;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJobId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SSHKeyOperationServiceTest {

	@Mock
	private SSHKeyOperationRepository sshKeyOperationRepository;
	@Mock
	private SSHKeyRepository sshKeysRepository;
	@Mock
	private SSHKeyHistoryRepository sshKeyHistoryRepository;
	@Mock
	private InstalledSSHKeyRepository installedSSHKeyRepository;
	
	
	private SSHKeyOperationServiceImpl service;

	@BeforeEach
	void setUp() {

		service = new SSHKeyOperationServiceImpl(sshKeyOperationRepository, sshKeysRepository,
				sshKeyHistoryRepository, installedSSHKeyRepository);
	}

	@Test
	void shouldBlockUpdateWhenStatusIsTerminated() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		when(sshKeyOperationRepository.findByCorrelationId(correlationId))
				.thenReturn(SSHKeyOperationJob.builder().id(id.id.toString()).correlationId(correlationId)
						.status(SSHKeyOperationStatus.FAILED).build());

		assertThrows(IllegalStateTransitionException.class, () -> service.updateStatus(correlationId, new SSHKeyOperationResult(SSHKeyOperationStatus.ACK,
				new SSHKeyOperationError(null, null))));

		verify(sshKeyOperationRepository, times(0)).update(eq(id), eq(SSHKeyOperationStatus.ACK),
				eq(Optional.empty()), any());

	}

	@Test
	void shouldUpdateAndAddInstalledKey() {
		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));

		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();

		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.ADD)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));

		service.updateStatus(correlationId, new SSHKeyOperationResult(SSHKeyOperationStatus.DONE,
				new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		ArgumentCaptor<InstalledSSHKey> installedKey = ArgumentCaptor.forClass(InstalledSSHKey.class);

		verify(installedSSHKeyRepository, times(1)).create(installedKey.capture());
		assertThat(installedKey.getValue().value).isEqualTo(key.value);
		assertThat(installedKey.getValue().siteId).isEqualTo(siteId);
	}

	@Test
	void shouldUpdateAndRemoveInstalledKey() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();

		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.REMOVE)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));

		service.updateStatus(correlationId, new SSHKeyOperationResult(SSHKeyOperationStatus.DONE,
				new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		verify(installedSSHKeyRepository, times(1)).deleteBySSHKeyIdAndSiteId(keyId, siteId);
	}
	
	@Test
	void shouldUpdateAndUpdateInstalledKey() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();

		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.UPDATE)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));

		service.updateStatus(correlationId,
				new SSHKeyOperationResult(SSHKeyOperationStatus.DONE, new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		

		verify(installedSSHKeyRepository, times(1)).update(siteId,keyId, key.value);
	}
	
	@Test
	void shouldUpdateStatusAndAddHistoryWhenAddKey() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();

		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.ADD)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));

		service.updateStatus(correlationId,
				new SSHKeyOperationResult(SSHKeyOperationStatus.DONE, new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		ArgumentCaptor<SSHKeyHistory> history = ArgumentCaptor.forClass(SSHKeyHistory.class);

		verify(sshKeyHistoryRepository, times(1)).create(history.capture());
		assertThat(history.getValue().sshkeyFingerprint).isEqualTo(key.getFingerprint());
		assertThat(history.getValue().siteId).isEqualTo(siteId);

	}

	@Test
	void shouldUpdateStatusAndAddHistoryWhenUpdateKey() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();

		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.UPDATE)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));

		when(sshKeyHistoryRepository.findBySiteIdAndOwnerIdLimitTo(siteId, "id", 1)).thenReturn(Collections.singletonList(SSHKeyHistory.builder().sshkeyFingerprint("xxx").build()));
		
		service.updateStatus(correlationId,
				new SSHKeyOperationResult(SSHKeyOperationStatus.DONE, new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		ArgumentCaptor<SSHKeyHistory> history = ArgumentCaptor.forClass(SSHKeyHistory.class);

		verify(sshKeyHistoryRepository, times(1)).create(history.capture());
		assertThat(history.getValue().sshkeyFingerprint).isEqualTo(key.getFingerprint());
		assertThat(history.getValue().siteId).isEqualTo(siteId);

	}
	
	@Test
	void shouldUpdateAndRemoveKey() {

		CorrelationId correlationId = CorrelationId.randomID();
		SSHKeyOperationJobId id = new SSHKeyOperationJobId(UUID.randomUUID());
		SSHKeyId keyId = new SSHKeyId(UUID.randomUUID());
		SiteId siteId = new SiteId(UUID.randomUUID().toString(), new SiteExternalId("id"));
		SSHKey key = SSHKey.builder().id(keyId).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
						+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
						+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
						+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
						+ "mb9xUkdFjZk9bL demo@demo.pl")
				.ownerId(new PersistentId("id")).sites(Sets.newHashSet(siteId)).build();
		when(sshKeysRepository.findById(keyId)).thenReturn(Optional.of(key));
		when(sshKeyOperationRepository.findByCorrelationId(correlationId)).thenReturn(SSHKeyOperationJob
				.builder().id(id.id.toString()).correlationId(correlationId).operation(SSHKeyOperation.REMOVE)
				.status(SSHKeyOperationStatus.ACK).sshkeyId(keyId).siteId(siteId).build());

		service.updateStatus(correlationId,
				new SSHKeyOperationResult(SSHKeyOperationStatus.DONE, new SSHKeyOperationError(null, null)));

		verify(sshKeyOperationRepository, times(1)).update(eq(id), eq(SSHKeyOperationStatus.DONE),
				eq(Optional.empty()), any());

		verify(sshKeysRepository).delete(keyId);

	}

}
