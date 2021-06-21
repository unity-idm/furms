/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import static java.util.UUID.fromString;
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
import org.springframework.data.domain.PageRequest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.sites.SiteRepository;

@SpringBootTest
class SSHKeyHistoryEntityRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyHistoryEntityRepository entityRepository;

	private UUID siteId;

	@BeforeEach
	void init() throws IOException {
		Site site = Site.builder().name("name").build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));
	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		siteRepository.deleteAll();
	}

	@Test
	void shouldCreateSSHKeyHistory() {
		// given

		SSHKeyHistoryEntity entityToSave = SSHKeyHistoryEntity.builder().siteId(siteId).sshkeyOwnerId("owner")
				.sshkeyFingerprint("fingerprint").originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyHistoryEntity saved = entityRepository.save(entityToSave);

		// then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<SSHKeyHistoryEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().sshkeyFingerprint).isEqualTo(saved.sshkeyFingerprint);
	}

	@Test
	void shouldFindSSHKeyHistoryBySiteIdLimitTo2() {
		// given
		SSHKeyHistoryEntity entityToSave1 = SSHKeyHistoryEntity.builder().siteId(siteId).sshkeyOwnerId("owner")
				.sshkeyFingerprint("fingerprint1")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();
		SSHKeyHistoryEntity entityToSave2 = SSHKeyHistoryEntity.builder().siteId(siteId).sshkeyOwnerId("owner")
				.sshkeyFingerprint("fingerprint2")
				.originationTime(LocalDateTime.now().withSecond(2).withNano(0)).build();
		SSHKeyHistoryEntity entityToSave3 = SSHKeyHistoryEntity.builder().siteId(siteId).sshkeyOwnerId("owner")
				.sshkeyFingerprint("fingerprint3")
				.originationTime(LocalDateTime.now().withSecond(3).withNano(0)).build();

		entityRepository.save(entityToSave1);
		SSHKeyHistoryEntity saved2 = entityRepository.save(entityToSave2);
		SSHKeyHistoryEntity saved3 = entityRepository.save(entityToSave3);

		// when
		List<SSHKeyHistoryEntity> findBysiteIdOrderByOriginationTime = entityRepository
				.findBysiteIdAndSshkeyOwnerIdOrderByOriginationTimeDesc(siteId.toString(), "owner",
						PageRequest.of(0, 2));

		// then
		assertThat(findBysiteIdOrderByOriginationTime).hasSize(2);
		assertThat(findBysiteIdOrderByOriginationTime).hasSameElementsAs(Sets.newSet(saved2, saved3));
	}

	@Test
	void shouldDeleteAllSSHKeyHistories() {
		// given
		SSHKeyHistoryEntity entityToSave1 = SSHKeyHistoryEntity.builder().siteId(siteId)
				.sshkeyFingerprint("fingerprint1").sshkeyOwnerId("owner")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();
		entityRepository.save(entityToSave1);
		// when
		entityRepository.deleteAll();

		// then
		assertThat(entityRepository.count()).isEqualTo(0);
	}

	@Test
	void shouldDeleteOnlyLast5SSHKeyHistory() {
		// given

		for (int i = 0; i < 10; i++) {
			SSHKeyHistoryEntity entityToSave = SSHKeyHistoryEntity.builder().siteId(siteId)
					.sshkeyOwnerId("owner").sshkeyFingerprint("fingerprint" + i)
					.originationTime(LocalDateTime.now().withSecond(i).withNano(0)).build();
			entityRepository.save(entityToSave);
		}
		// when
		entityRepository.deleteOldestLeaveOnly(fromString(siteId.toString()), "owner", 5);
		List<SSHKeyHistoryEntity> findAll = entityRepository
				.findBysiteIdAndSshkeyOwnerIdOrderByOriginationTimeDesc(siteId.toString(), "owner",
						PageRequest.of(0, 1000));

		assertThat(findAll.size()).isEqualTo(5);
		assertThat(findAll.get(0).sshkeyFingerprint).isEqualTo("fingerprint9");
		assertThat(findAll.get(1).sshkeyFingerprint).isEqualTo("fingerprint8");
		assertThat(findAll.get(2).sshkeyFingerprint).isEqualTo("fingerprint7");
		assertThat(findAll.get(3).sshkeyFingerprint).isEqualTo("fingerprint6");
		assertThat(findAll.get(4).sshkeyFingerprint).isEqualTo("fingerprint5");
	}
	
	@Test
	void shouldDeleteLatest() {
		// given

		for (int i = 0; i < 10; i++) {
			SSHKeyHistoryEntity entityToSave = SSHKeyHistoryEntity.builder().siteId(siteId)
					.sshkeyOwnerId("owner").sshkeyFingerprint("fingerprint" + i)
					.originationTime(LocalDateTime.now().withSecond(i).withNano(0)).build();
			entityRepository.save(entityToSave);
		}
		// when
		entityRepository.deleteLatest(fromString(siteId.toString()), "owner");
		List<SSHKeyHistoryEntity> findAll = entityRepository
				.findBysiteIdAndSshkeyOwnerIdOrderByOriginationTimeDesc(siteId.toString(), "owner",
						PageRequest.of(0, 1000));

		assertThat(findAll.size()).isEqualTo(9);
		assertThat(findAll.get(0).sshkeyFingerprint).isEqualTo("fingerprint8");
		assertThat(findAll.get(1).sshkeyFingerprint).isEqualTo("fingerprint7");
		assertThat(findAll.get(2).sshkeyFingerprint).isEqualTo("fingerprint6");
		assertThat(findAll.get(3).sshkeyFingerprint).isEqualTo("fingerprint5");
		assertThat(findAll.get(4).sshkeyFingerprint).isEqualTo("fingerprint4");
		assertThat(findAll.get(5).sshkeyFingerprint).isEqualTo("fingerprint3");
		assertThat(findAll.get(6).sshkeyFingerprint).isEqualTo("fingerprint2");
		assertThat(findAll.get(7).sshkeyFingerprint).isEqualTo("fingerprint1");
		assertThat(findAll.get(8).sshkeyFingerprint).isEqualTo("fingerprint0");
		
	}

	@Test
	void shouldFindCreatedSSHKeyHistory() {
		// given
		SSHKeyHistoryEntity entityToSave = SSHKeyHistoryEntity.builder().siteId(siteId)
				.sshkeyFingerprint("fingerprint1").sshkeyOwnerId("owner")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();

		entityRepository.save(entityToSave);

		// when
		Optional<SSHKeyHistoryEntity> byId = entityRepository.findById(entityToSave.getId());

		// then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindAllAvailableSSHKeyHistories() {
		// given
		SSHKeyHistoryEntity entityToSave1 = SSHKeyHistoryEntity.builder().siteId(siteId)
				.sshkeyFingerprint("fingerprint1").sshkeyOwnerId("owner")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();
		SSHKeyHistoryEntity entityToSave2 = SSHKeyHistoryEntity.builder().siteId(siteId)
				.sshkeyFingerprint("fingerprint2").sshkeyOwnerId("owner")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();
		entityRepository.save(entityToSave1);
		entityRepository.save(entityToSave2);

		// when
		Iterable<SSHKeyHistoryEntity> all = entityRepository.findAll();

		// then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteSSHKeyHistory() {
		// given
		SSHKeyHistoryEntity toSave = SSHKeyHistoryEntity.builder().siteId(siteId)
				.sshkeyFingerprint("fingerprint1").sshkeyOwnerId("owner")
				.originationTime(LocalDateTime.now().withSecond(1).withNano(0)).build();

		// when
		entityRepository.save(toSave);
		entityRepository.deleteById(toSave.getId());

		// then
		assertThat(entityRepository.findById(toSave.getId())).isEmpty();
	}
}