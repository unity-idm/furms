/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.ACK;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.SEND;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperation.ADD;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.db.ssh_key_operation.SSHKeyOperationJobEntity;
import io.imunity.furms.db.ssh_key_operation.SSHKeyOperationJobEntityRepository;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@SpringBootTest
class SSHKeyInstallationEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyRepository sshKeysRepository;

	@Autowired
	private SSHKeyOperationJobEntityRepository entityRepository;

	private UUID siteId;
	private UUID siteId1;

	private UUID sshkeyId;
	private UUID sshkeyId1;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder().name("name").build();
		Site site1 = Site.builder().name("name1").build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
		siteId1 = UUID.fromString(siteRepository.create(site1, new SiteExternalId("id1")));

		sshkeyId = UUID.fromString(sshKeysRepository.create(SSHKey.builder().createTime(LocalDateTime.now())
				.name("key").ownerId(new PersistentId("")).sites(Sets.newSet(siteId.toString()))
				.value("v").build()));

		sshkeyId1 = UUID.fromString(sshKeysRepository.create(SSHKey.builder().createTime(LocalDateTime.now())
				.name("key1").ownerId(new PersistentId("")).sites(Sets.newSet(siteId1.toString()))
				.value("v").build()));

	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		siteRepository.deleteAll();
		sshKeysRepository.deleteAll();
	}

	@Test
	void shouldCreateSSHKeyInstallationJob() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity entityToSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();

		// when
		SSHKeyOperationJobEntity saved = entityRepository.save(entityToSave);

		// then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().status).isEqualTo(SEND);
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldUpdateSSHKeyInstallationJob() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity entityToSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();

		// when
		SSHKeyOperationJobEntity save = entityRepository.save(entityToSave);

		SSHKeyOperationJobEntity entityToUpdate = SSHKeyOperationJobEntity.builder().id(save.getId())
				.correlationId(save.correlationId).siteId(save.siteId).sshkeyId(sshkeyId).operation(ADD)
				.status(ACK).build();

		entityRepository.save(entityToUpdate);

		// then
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(entityToSave.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(save.getId());
		assertThat(byId.get().status).isEqualTo(ACK);
		assertThat(byId.get().correlationId).isEqualTo(correlationId);
	}

	@Test
	void shouldFindCreatedSSHKeyInstallationJob() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity entityToSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();

		entityRepository.save(entityToSave);

		// when
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(entityToSave.getId());

		// then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedSSHKeyInstallationJobByCorrelationId() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toFind = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();

		entityRepository.save(toFind);
		SSHKeyOperationJobEntity findById = entityRepository.findByCorrelationId(correlationId);

		// when
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(findById.getId());

		// then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableSSHKeyInstallationJob() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();
		UUID correlationId1 = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave1 = SSHKeyOperationJobEntity.builder().correlationId(correlationId1)
				.siteId(siteId1).sshkeyId(sshkeyId1).operation(ADD).status(SEND).build();
		entityRepository.save(toSave);
		entityRepository.save(toSave1);

		// when
		Iterable<SSHKeyOperationJobEntity> all = entityRepository.findAll();

		// then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteSSHKeyInstallationJob() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();

		// when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		// then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllSSHKeyInstallationJobs() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();
		UUID correlationId1 = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave1 = SSHKeyOperationJobEntity.builder().correlationId(correlationId1)
				.siteId(siteId1).sshkeyId(sshkeyId1).operation(ADD).status(SEND).build();
		// when
		entityRepository.save(toSave);
		entityRepository.save(toSave1);
		entityRepository.deleteAll();

		// then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldFindByKeyAndSiteIdSSHKeyInstallationJobs() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND).build();
		// when
		entityRepository.save(toSave);

		// then
		assertThat(entityRepository.findBySshkeyIdAndSiteId(sshkeyId, siteId)).isEqualTo(toSave);
	}

	@Test
	void shouldDeleteByKeyAndSiteIdSSHKeyInstallationJobs() {
		// given
		UUID correlationId = UUID.randomUUID();
		SSHKeyOperationJobEntity toSave = SSHKeyOperationJobEntity.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(ACK).build();
		entityRepository.save(toSave);
		// when
		entityRepository.deleteBySshKeyIdAndSiteId(sshkeyId, siteId);

		// then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

}