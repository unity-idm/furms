/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@SpringBootTest
class SSHKeyHistoryDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyHistoryEntityRepository entityRepository;

	@Autowired
	private SSHKeyHistoryDatabaseRepository entityDatabaseRepository;

	@Autowired
	private SSHKeyRepository sshKeyRepository;

	private UUID siteId;

	@BeforeEach
	void init() {
		sshKeyRepository.deleteAll();
		siteRepository.deleteAll();
		Site site = Site.builder().name("name").build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		sshKeyRepository.deleteAll();
	}

	@Test
	void shouldCreateSSHKeyHistory() {
		// given
		SSHKeyHistory entityToSave = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint")
				.originationTime(LocalDateTime.now()).build();

		// when
		String id = entityDatabaseRepository.create(entityToSave);

		// then
		Optional<SSHKeyHistoryEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);
		assertThat(byId.get().sshkeyFingerprint).isEqualTo("fingerprint");
	}

	@Test
	void shouldFindBySiteIdAndOwnerIdLimitTo2() {
		// given
		SSHKeyHistory entityToSave1 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint1")
				.originationTime(LocalDateTime.now()).build();
		SSHKeyHistory entityToSave2 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint2")
				.originationTime(LocalDateTime.now().plusMinutes(1)).build();
		SSHKeyHistory entityToSave3 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint3")
				.originationTime(LocalDateTime.now().plusMinutes(2)).build();

		entityDatabaseRepository.create(entityToSave1);
		String saved2 = entityDatabaseRepository.create(entityToSave2);
		String saved3 = entityDatabaseRepository.create(entityToSave3);

		Optional<SSHKeyHistoryEntity> savedEntity2 = entityRepository.findById(UUID.fromString(saved2));
		Optional<SSHKeyHistoryEntity> savedEntity3 = entityRepository.findById(UUID.fromString(saved3));

		// when
		List<SSHKeyHistory> findBysiteIdOrderByOriginationTime = entityDatabaseRepository
				.findBySiteIdAndOwnerIdLimitTo(siteId.toString(), "owner", 2);

		// then
		assertThat(findBysiteIdOrderByOriginationTime).hasSize(2);
		assertThat(findBysiteIdOrderByOriginationTime).hasSameElementsAs(Sets
				.newSet(savedEntity2.get().toSSHKeyHistory(), savedEntity3.get().toSSHKeyHistory()));
	}
	
	@Test
	void shouldRemoveLatest() {
		// given
		SSHKeyHistory entityToSave1 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint1")
				.originationTime(LocalDateTime.now()).build();
		SSHKeyHistory entityToSave2 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint2")
				.originationTime(LocalDateTime.now().plusMinutes(1)).build();
		SSHKeyHistory entityToSave3 = SSHKeyHistory.builder().siteId(siteId.toString())
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint3")
				.originationTime(LocalDateTime.now().plusMinutes(2)).build();

		String saved1 =  entityDatabaseRepository.create(entityToSave1);
		String saved2 = entityDatabaseRepository.create(entityToSave2);
		entityDatabaseRepository.create(entityToSave3);

		Optional<SSHKeyHistoryEntity> savedEntity1 = entityRepository.findById(UUID.fromString(saved1));
		Optional<SSHKeyHistoryEntity> savedEntity2 = entityRepository.findById(UUID.fromString(saved2));
		
		entityDatabaseRepository.deleteLatest(siteId.toString(), "owner");
		
		List<SSHKeyHistory> findBysiteIdOrderByOriginationTime = entityDatabaseRepository
				.findBySiteIdAndOwnerIdLimitTo(siteId.toString(), "owner", 10);
		assertThat(findBysiteIdOrderByOriginationTime).hasSize(2);
		assertThat(findBysiteIdOrderByOriginationTime).hasSameElementsAs(Sets
				.newSet(savedEntity1.get().toSSHKeyHistory(), savedEntity2.get().toSSHKeyHistory()));
		
		
	}

	@Test
	void shouldDeleteOnlyLast5() {
		// given

		for (int i = 0; i < 10; i++) {
			SSHKeyHistory entityToSave = SSHKeyHistory.builder().siteId(siteId.toString())
					.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint" + i)
					.originationTime(LocalDateTime.now().withSecond(i).withNano(0)).build();
			entityDatabaseRepository.create(entityToSave);
		}
		// when
		entityDatabaseRepository.deleteOldestLeaveOnly(siteId.toString(), "owner", 5);

		// then
		List<SSHKeyHistory> findAll = entityDatabaseRepository.findBySiteIdAndOwnerIdLimitTo(siteId.toString(),
				"owner", 1000);

		assertThat(findAll.size()).isEqualTo(5);
		assertThat(findAll.get(0).sshkeyFingerprint).isEqualTo("fingerprint9");
		assertThat(findAll.get(1).sshkeyFingerprint).isEqualTo("fingerprint8");
		assertThat(findAll.get(2).sshkeyFingerprint).isEqualTo("fingerprint7");
		assertThat(findAll.get(3).sshkeyFingerprint).isEqualTo("fingerprint6");
		assertThat(findAll.get(4).sshkeyFingerprint).isEqualTo("fingerprint5");
	}

}