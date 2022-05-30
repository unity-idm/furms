/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJobId;
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
import java.util.UUID;

import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.SEND;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SSHKeyOperationDatabaseRepositoryTest extends DBIntegrationTest {
	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyOperationJobEntityRepository entityRepository;

	@Autowired
	private SSHKeyOperationJobDatabaseRepository entityDatabaseRepository;

	@Autowired
	private SSHKeyRepository sshKeyRepository;

	private SiteId siteId;

	private SSHKeyId sshkeyId;

	@BeforeEach
	void init() {
		sshKeyRepository.deleteAll();
		siteRepository.deleteAll();
		Site site = Site.builder().name("name").build();
		siteId = siteRepository.create(site, new SiteExternalId("id"));

		sshkeyId = sshKeyRepository.create(SSHKey.builder().createTime(LocalDateTime.now())
				.name("key").ownerId(new PersistentId("")).sites(Sets.newSet(siteId))
				.value("v").build());

	}

	@AfterEach
	void clean() {
		entityRepository.deleteAll();
		sshKeyRepository.deleteAll();
	}

	@Test
	void shouldCreateSSHKeyOperation() {
		// given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request = SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyOperationJobId id = entityDatabaseRepository.create(request);

		// then
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(id.id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id.id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(SEND);
		assertThat(byId.get().operation).isEqualTo(ADD);

	}

	@Test
	void shouldUpdateSSHKeyOperation() {
		// given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request = SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(siteId).operation(ADD).sshkeyId(sshkeyId).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyOperationJobId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.update(id, DONE, Optional.empty(), LocalDateTime.now());

		// then
		Optional<SSHKeyOperationJobEntity> byId = entityRepository.findById(id.id);
		assertThat(byId).isPresent();
		assertThat(byId.get().getId()).isEqualTo(id.id);
		assertThat(byId.get().correlationId.toString()).isEqualTo(correlationId.id);
		assertThat(byId.get().status).isEqualTo(DONE);
	}

	@Test
	void shouldRemoveSSHKeyOperation() {
		// given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request = SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyOperationJobId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.delete(id);

		// then
		assertThat(entityRepository.findById(id.id)).isEmpty();
	}

	@Test
	void shouldRemoveBySiteAndKeyId() {
		// given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request = SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		SSHKeyOperationJobId id = entityDatabaseRepository.create(request);
		entityDatabaseRepository.deleteBySSHKeyIdAndSiteId(sshkeyId, siteId);

		// then
		assertThat(entityRepository.findById(id.id)).isEmpty();
	}

	@Test
	void shouldFindBySSHKeyIdAndSiteId() {
		// given
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request = SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		entityDatabaseRepository.create(request);

		// then
		SSHKeyOperationJob foundByKeyAndSite = entityDatabaseRepository.findBySSHKeyIdAndSiteId(sshkeyId, siteId);

		assertThat(foundByKeyAndSite.siteId).isEqualTo(siteId);
		assertThat(foundByKeyAndSite.sshkeyId).isEqualTo(sshkeyId);
	}

	@Test
	void shouldFindBySSHKey() {
		// given
		CorrelationId correlationId1 = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request1 = SSHKeyOperationJob.builder().correlationId(correlationId1)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD)
				.originationTime(LocalDateTime.now()).status(SEND).build();
		Site site2 = Site.builder().name("name2").build();
		SiteId siteId2 = siteRepository.create(site2, new SiteExternalId("id2"));
		CorrelationId correlationId2 = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request2 = SSHKeyOperationJob.builder().correlationId(correlationId2)
				.siteId(siteId2).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		entityDatabaseRepository.create(request1);
		entityDatabaseRepository.create(request2);

		// then
		List<SSHKeyOperationJob> foundByKeyAndSite = entityDatabaseRepository.findBySSHKey(sshkeyId);

		assertThat(foundByKeyAndSite).hasSize(2);

	}
	
	@Test
	void shouldFindByStatus() {
		// given
		CorrelationId correlationId1 = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request1 = SSHKeyOperationJob.builder().correlationId(correlationId1)
				.siteId(siteId).sshkeyId(sshkeyId).operation(ADD)
				.originationTime(LocalDateTime.now()).status(SEND).build();
		Site site2 = Site.builder().name("name2").build();
		SiteId siteId2 = siteRepository.create(site2, new SiteExternalId("id2"));
		CorrelationId correlationId2 = new CorrelationId(UUID.randomUUID().toString());
		SSHKeyOperationJob request2 = SSHKeyOperationJob.builder().correlationId(correlationId2)
				.siteId(siteId2).sshkeyId(sshkeyId).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build();

		// when
		entityDatabaseRepository.create(request1);
		entityDatabaseRepository.create(request2);

		// then
		List<SSHKeyOperationJob> foundByKeyAndSite = entityDatabaseRepository.findByStatus(SEND);

		assertThat(foundByKeyAndSite).hasSize(2);
	}

}