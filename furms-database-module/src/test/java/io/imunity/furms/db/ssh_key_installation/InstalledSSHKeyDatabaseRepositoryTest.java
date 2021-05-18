/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
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

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@SpringBootTest
class InstalledSSHKeyDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private InstalledSSHKeyEntityRepository entityRepository;

	@Autowired
	private InstalledSSHKeyDatabaseRepository entityDatabaseRepository;

	@Autowired
	private SSHKeyRepository sshKeyRepository;

	private UUID siteId;

	private UUID sshkeyId;

	@BeforeEach
	void init() throws IOException {
		sshKeyRepository.deleteAll();
		siteRepository.deleteAll();
		Site site = Site.builder().name("name").build();
		siteId = UUID.fromString(siteRepository.create(site, new SiteExternalId("id")));

		sshkeyId = UUID.fromString(sshKeyRepository.create(SSHKey.builder().createTime(LocalDateTime.now())
				.name("key").ownerId(new PersistentId("")).sites(Sets.newSet(siteId.toString()))
				.value("v").build()));

	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		sshKeyRepository.deleteAll();
	}

	@Test
	void shouldCreateInstalledKey() {
		// given
		InstalledSSHKey request = InstalledSSHKey.builder().siteId(siteId.toString())
				.sshkeyId(sshkeyId.toString()).value("v").build();

		// when
		String id = entityDatabaseRepository.create(request);

		// then
		Optional<InstalledSSHKeyEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);

	}

	@Test
	void shouldUpdateSSHKeyOperation() {
		// given
		InstalledSSHKey request = InstalledSSHKey.builder().siteId(siteId.toString())
				.sshkeyId(sshkeyId.toString()).value("v").build();

		// when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(siteId.toString(), sshkeyId.toString(), "v2");

		// then
		Optional<InstalledSSHKeyEntity> byId = entityRepository.findById(UUID.fromString(id));
		assertThat(byId).isPresent();
		assertThat(byId.get().getId().toString()).isEqualTo(id);

		assertThat(byId.get().value).isEqualTo("v2");
	}

	@Test
	void shouldRemoveSSHKeyOperation() {
		// given
		InstalledSSHKey request = InstalledSSHKey.builder().siteId(siteId.toString())
				.sshkeyId(sshkeyId.toString()).value("v").build();

		// when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.delete(id);

		// then
		assertThat(entityRepository.findById(UUID.fromString(id))).isEmpty();
	}

	@Test
	void shouldRemoveBySiteAndKeyId() {
		// given
		InstalledSSHKey request = InstalledSSHKey.builder().siteId(siteId.toString())
				.sshkeyId(sshkeyId.toString()).value("v").build();

		// when
		String id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.deleteBySSHKeyIdAndSiteId(sshkeyId.toString(), siteId.toString());

		// then
		assertThat(entityRepository.findById(UUID.fromString(id))).isEmpty();
	}

	@Test
	void shouldFindBySSHKey() {
		// given
		InstalledSSHKey request = InstalledSSHKey.builder().siteId(siteId.toString())
				.sshkeyId(sshkeyId.toString()).value("v").build();

		// when
		entityDatabaseRepository.create(request);

		// then
		List<InstalledSSHKey> foundByKeyAndSite = entityDatabaseRepository.findBySSHKeyId(sshkeyId.toString());

		assertThat(foundByKeyAndSite.size()).isEqualTo(1);
		assertThat(foundByKeyAndSite.get(0).siteId).isEqualTo(siteId.toString());
		assertThat(foundByKeyAndSite.get(0).sshkeyId).isEqualTo(sshkeyId.toString());
	}
}