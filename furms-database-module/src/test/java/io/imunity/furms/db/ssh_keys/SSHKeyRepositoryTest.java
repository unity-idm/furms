/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static java.time.Clock.systemUTC;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.spi.sites.SiteRepository;

@SpringBootTest
public class SSHKeyRepositoryTest {
	@Autowired
	private SSHKeyEntityRepository sshKeyEntityRepository;
	@Autowired
	private SiteRepository siteRep;

	private UUID site1Id;
	private UUID site2Id;

	@BeforeEach
	void setUp() {
		sshKeyEntityRepository.deleteAll();
		siteRep.deleteAll();
		site1Id = UUID.fromString(siteRep.create(Site.builder().name("s1").connectionInfo("alala").build(),
				new SiteExternalId("s1")));
		site2Id = UUID.fromString(siteRep.create(Site.builder().name("s2").connectionInfo("alala").build(),
				new SiteExternalId("s2")));
	}

	@Test
	void shouldCreateSSHKeyEntity() {

		// given
		SSHKeyEntity entityToSave = SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build();

		// when
		SSHKeyEntity saved = sshKeyEntityRepository.save(entityToSave);

		// then
		assertThat(sshKeyEntityRepository.findAll()).hasSize(1);
		assertThat(sshKeyEntityRepository.findById(saved.getId())).isPresent();
		assertThat(sshKeyEntityRepository.findById(saved.getId()).get().getSites()).hasSameElementsAs(
				Sets.newSet(new SSHKeySiteReference(site1Id), new SSHKeySiteReference(site2Id)));
	}

	@Test
	void shouldUpdateSSHKeyEntity() {
		// given
		SSHKeyEntity old = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());
		SSHKeyEntity toUpdate = SSHKeyEntity.builder().id(old.getId()).name("new_name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).build();

		// when
		sshKeyEntityRepository.save(toUpdate);

		// then
		Optional<SSHKeyEntity> byId = sshKeyEntityRepository.findById(toUpdate.getId());
		assertThat(byId).isPresent();
		assertThat(byId.get().getName()).isEqualTo("new_name");
	}

	@Test
	void shouldFindSSHKeyById() {
		// given
		SSHKeyEntity toFind = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when
		Optional<SSHKeyEntity> byId = sshKeyEntityRepository.findById(toFind.getId());

		// then
		assertThat(byId).isPresent();
	}

	@Test
	void shouldFindOwnerById() {
		// given
		SSHKeyEntity toFind = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now(systemUTC()))
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build());

		// when
		Set<SSHKeyEntity> byOwner = sshKeyEntityRepository.findAllByOwnerId("o1");

		// then
		assertThat(byOwner.stream().findFirst().get().getId()).isEqualTo(toFind.getId());
	}

	@Test
	void shouldFindAllAvailableSSHKeys() {
		// given
		sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name1").value("v1").ownerId("o1")
				.createTime(LocalDateTime.now()).build());
		sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name2").value("v2").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when
		Iterable<SSHKeyEntity> all = sshKeyEntityRepository.findAll();

		// then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCheckIfExistsBySSHKeyId() {
		// given
		SSHKeyEntity sshKey = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name1").value("v1")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(sshKeyEntityRepository.existsById(sshKey.getId())).isTrue();
		assertThat(sshKeyEntityRepository.existsById(generateId())).isFalse();
	}

	@Test
	void shouldDeleteEntity() {
		// given
		SSHKeyEntity entityToRemove = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name1")
				.value("v1").ownerId("o1").createTime(LocalDateTime.now()).build());

		// when
		sshKeyEntityRepository.deleteById(entityToRemove.getId());

		// then
		assertThat(sshKeyEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldDeleteAllEntities() {
		// given
		sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name1").value("v1").ownerId("o1")
				.createTime(LocalDateTime.now()).build());
		sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name2").value("v2").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when
		sshKeyEntityRepository.deleteAll();

		// then
		assertThat(sshKeyEntityRepository.findAll()).hasSize(0);
	}

	@Test
	void shouldRemoveOneSiteReference() {
		// given
		SSHKeyEntity entityToSave = SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build();

		// when
		SSHKeyEntity saved = sshKeyEntityRepository.save(entityToSave);

		// then
		assertThat(sshKeyEntityRepository.findById(saved.getId()).get().getSites()).hasSize(2);

		// when
		siteRep.delete(site1Id.toString());

		// then
		assertThat(sshKeyEntityRepository.findAll()).hasSize(1);
		assertThat(sshKeyEntityRepository.findById(saved.getId())).isPresent();
		assertThat(sshKeyEntityRepository.findById(saved.getId()).get().getSites()).hasSameElementsAs(
				Sets.newSet(new SSHKeySiteReference(UUID.fromString(site2Id.toString()))));
	}

	@Test
	void shouldReturnTrueIfNameIsPresentOutOfSpecificRecord() {
		// given
		SSHKeyEntity entity = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());
		SSHKeyEntity entity2 = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name2").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(sshKeyEntityRepository.existsByNameAndIdIsNot(entity.getName(), entity2.getId())).isTrue();
	}

	@Test
	void shouldReturnFalseIfNameIsPresentOnlyInSpecificRecord() {
		// given
		SSHKeyEntity entity = sshKeyEntityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(sshKeyEntityRepository.existsByNameAndIdIsNot("otherName", entity.getId())).isFalse();
	}
}
