/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_key_installation;

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

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@SpringBootTest
public class InstalledSSHKeyEntityRepositoryTest {
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyRepository sshKeysRepository;

	@Autowired
	private InstalledSSHKeyEntityRepository entityRepository;

	private UUID siteId;
	private UUID siteId1;

	private UUID sshkeyId;
	private UUID sshkeyId1;

	@BeforeEach
	void init() {
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
	void shouldCreateInstalledKey() {
		// given

		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();

		// when
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// then
		assertThat(entityRepository.findAll()).hasSize(1);
		Optional<InstalledSSHKeyEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());

	}

	@Test
	void shouldUpdateInstalledKey() {
		// given
		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// when
		InstalledSSHKeyEntity entityToUpdate = InstalledSSHKeyEntity.builder().id(saved.getId())
				.siteId(saved.siteId).sshkeyId(sshkeyId).sshkeyId(saved.sshkeyId).value("y").build();

		entityRepository.save(entityToUpdate);

		// then
		Optional<InstalledSSHKeyEntity> byId = entityRepository.findById(saved.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(saved.getId());
		assertThat(byId.get().value).isEqualTo("y");

	}

	@Test
	void shouldFindCreatedInstalledKey() {
		// given
		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// when
		Optional<InstalledSSHKeyEntity> byId = entityRepository.findById(saved.getId());

		// then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindCreatedInstalledKeyByKeyId() {
		// given
		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// when
		List<InstalledSSHKeyEntity> byId = entityRepository.findBySshkeyId(sshkeyId);

		// then
		assertThat(byId.size()).isEqualTo(1);
		assertThat(byId.get(0).getId()).isEqualTo(saved.getId());
	}

	@Test
	void shouldFindAllInstalledKey() {
		// given
		InstalledSSHKeyEntity request1 = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		entityRepository.save(request1);
		InstalledSSHKeyEntity request2 = InstalledSSHKeyEntity.builder().siteId(siteId1).sshkeyId(sshkeyId1)
				.value("y").build();
		entityRepository.save(request2);

		// when
		Iterable<InstalledSSHKeyEntity> all = entityRepository.findAll();

		// then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldDeleteInstalledKey() {
		// given
		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// when

		entityRepository.deleteById(saved.getId());

		// then
		assertThat(entityRepository.findById(saved.getId())).isEmpty();
	}

	@Test
	void shouldDeleteAllInstalledKeys() {
		// given
		InstalledSSHKeyEntity request1 = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		entityRepository.save(request1);
		InstalledSSHKeyEntity request2 = InstalledSSHKeyEntity.builder().siteId(siteId1).sshkeyId(sshkeyId1)
				.value("y").build();
		entityRepository.save(request2);
		// when

		entityRepository.deleteAll();

		// then
		assertThat(entityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldFindByKeyAndSiteIdInstalledKey() {
		// given
		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();

		// when
		InstalledSSHKeyEntity saved = entityRepository.save(request);

		// then
		assertThat(entityRepository.findBySshkeyIdAndSiteId(sshkeyId, siteId).get()).isEqualTo(saved);
	}

	@Test
	void shouldDeleteByKeyAndSiteIdSSHInstalledKeys() {
		// given

		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		entityRepository.save(request);
		// when
		entityRepository.deleteBySshkeyIdAndSiteId(sshkeyId, siteId);

		// then
		assertThat(entityRepository.findAll()).hasSize(0);
	}
	
	@Test
	void shouldDeleteByKeySSHInstalledKeys() {
		// given

		InstalledSSHKeyEntity request = InstalledSSHKeyEntity.builder().siteId(siteId).sshkeyId(sshkeyId)
				.value("x").build();
		entityRepository.save(request);
		// when
		entityRepository.deleteBySshkeyId(sshkeyId);

		// then
		assertThat(entityRepository.findAll()).hasSize(0);
	}
}
