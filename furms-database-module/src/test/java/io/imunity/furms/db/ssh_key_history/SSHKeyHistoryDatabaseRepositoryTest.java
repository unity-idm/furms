/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistoryId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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

	private SiteId siteId;

	@BeforeEach
	void init() {
		sshKeyRepository.deleteAll();
		siteRepository.deleteAll();
		Site site = Site.builder().name("name").build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));

	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		sshKeyRepository.deleteAll();
	}

	@Test
	void shouldCreateSSHKeyHistory() {
		// given
		SSHKeyHistory entityToSave = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint")
				.originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyHistoryId id = entityDatabaseRepository.create(entityToSave);

		// then
		Optional<SSHKeyHistoryEntity> byId = entityRepository.findById(id.id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id.id);
		assertThat(byId.get().sshkeyFingerprint).isEqualTo("fingerprint");
	}

	@Test
	void shouldFindBySiteIdAndOwnerIdLimitTo2() {
		// given
		SSHKeyHistory entityToSave1 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint1")
				.originationTime(LocalDateTime.now()).build();
		SSHKeyHistory entityToSave2 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint2")
				.originationTime(LocalDateTime.now().plusMinutes(1)).build();
		SSHKeyHistory entityToSave3 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint3")
				.originationTime(LocalDateTime.now().plusMinutes(2)).build();

		entityDatabaseRepository.create(entityToSave1);
		SSHKeyHistoryId saved2 = entityDatabaseRepository.create(entityToSave2);
		SSHKeyHistoryId saved3 = entityDatabaseRepository.create(entityToSave3);

		Optional<SSHKeyHistoryEntity> savedEntity2 = entityRepository.findById(saved2.id);
		Optional<SSHKeyHistoryEntity> savedEntity3 = entityRepository.findById(saved3.id);

		// when
		List<SSHKeyHistory> findBysiteIdOrderByOriginationTime = entityDatabaseRepository
				.findBySiteIdAndOwnerIdLimitTo(siteId, "owner", 2);

		// then
		assertThat(findBysiteIdOrderByOriginationTime).hasSize(2);
		assertThat(findBysiteIdOrderByOriginationTime).hasSameElementsAs(Sets
				.newSet(savedEntity2.get().toSSHKeyHistory(), savedEntity3.get().toSSHKeyHistory()));
	}
	
	@Test
	void shouldRemoveLatest() {
		// given
		SSHKeyHistory entityToSave1 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint1")
				.originationTime(LocalDateTime.now()).build();
		SSHKeyHistory entityToSave2 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint2")
				.originationTime(LocalDateTime.now().plusMinutes(1)).build();
		SSHKeyHistory entityToSave3 = SSHKeyHistory.builder().siteId(siteId)
				.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint3")
				.originationTime(LocalDateTime.now().plusMinutes(2)).build();

		SSHKeyHistoryId saved1 =  entityDatabaseRepository.create(entityToSave1);
		SSHKeyHistoryId saved2 = entityDatabaseRepository.create(entityToSave2);
		entityDatabaseRepository.create(entityToSave3);

		Optional<SSHKeyHistoryEntity> savedEntity1 = entityRepository.findById(saved1.id);
		Optional<SSHKeyHistoryEntity> savedEntity2 = entityRepository.findById(saved2.id);
		
		entityDatabaseRepository.deleteLatest(siteId, "owner");
		
		List<SSHKeyHistory> findBysiteIdOrderByOriginationTime = entityDatabaseRepository
				.findBySiteIdAndOwnerIdLimitTo(siteId, "owner", 10);
		assertThat(findBysiteIdOrderByOriginationTime).hasSize(2);
		assertThat(findBysiteIdOrderByOriginationTime).hasSameElementsAs(Sets
				.newSet(savedEntity1.get().toSSHKeyHistory(), savedEntity2.get().toSSHKeyHistory()));
		
		
	}

	@Test
	void shouldDeleteOnlyLast5() {
		// given

		for (int i = 0; i < 10; i++) {
			SSHKeyHistory entityToSave = SSHKeyHistory.builder().siteId(siteId)
					.sshkeyOwnerId(new PersistentId("owner")).sshkeyFingerprint("fingerprint" + i)
					.originationTime(LocalDateTime.now().withSecond(i).withNano(0)).build();
			entityDatabaseRepository.create(entityToSave);
		}
		// when
		entityDatabaseRepository.deleteOldestLeaveOnly(siteId, "owner", 5);

		// then
		List<SSHKeyHistory> findAll = entityDatabaseRepository.findBySiteIdAndOwnerIdLimitTo(siteId,
				"owner", 1000);

		assertThat(findAll.size()).isEqualTo(5);
		assertThat(findAll.get(0).sshkeyFingerprint).isEqualTo("fingerprint9");
		assertThat(findAll.get(1).sshkeyFingerprint).isEqualTo("fingerprint8");
		assertThat(findAll.get(2).sshkeyFingerprint).isEqualTo("fingerprint7");
		assertThat(findAll.get(3).sshkeyFingerprint).isEqualTo("fingerprint6");
		assertThat(findAll.get(4).sshkeyFingerprint).isEqualTo("fingerprint5");
	}

}