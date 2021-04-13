/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static java.time.Clock.systemUTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.db.ssh_keys.SSHKeyDatabaseRepository;
import io.imunity.furms.db.ssh_keys.SSHKeyEntity;
import io.imunity.furms.db.ssh_keys.SSHKeyEntityRepository;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;

@SpringBootTest
public class SSHKeyDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyDatabaseRepository repository;

	@Autowired
	private SSHKeyEntityRepository entityRepository;

	private UUID site2Id;
	private UUID site1Id;

	@BeforeEach
	void setUp() {
		entityRepository.deleteAll();
		siteRepository.deleteAll();
		site1Id = UUID.fromString(siteRepository.create(
				Site.builder().name("s1").connectionInfo("alala").build(), new SiteExternalId("id1")));
		site2Id = UUID.fromString(siteRepository.create(
				Site.builder().name("s2").connectionInfo("alala").build(), new SiteExternalId("id2")));
	}

	@Test
	void shouldFindCreatedSSHKey() {
		// given
		SSHKeyEntity entity = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now(systemUTC())).updateTime(LocalDateTime.now(systemUTC()))
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build());

		// when
		Optional<SSHKey> byId = repository.findById(entity.getId().toString());

		// then
		assertThat(byId).isPresent();
		SSHKey key = byId.get();
		assertThat(entity.getId().toString()).isEqualTo(key.id.toString());
		assertThat(entity.getOwnerId()).isEqualTo(key.ownerId.id);
		assertThat(entity.getName()).isEqualTo(key.name);
		assertThat(entity.getValue()).isEqualTo(key.value);
		assertThat(entity.getSites().stream().map(s -> s.getSiteId().toString()).collect(Collectors.toSet()))
				.isEqualTo(key.sites);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		// given
		UUID wrongId = generateId();
		entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when
		Optional<SSHKey> byId = repository.findById(wrongId.toString());

		// then
		assertThat(byId).isEmpty();
	}

	@Test
	void shouldFindAllSSHKeys() {
		// given
		entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());
		entityRepository.save(SSHKeyEntity.builder().name("name2").value("v2").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when
		Set<SSHKey> all = repository.findAll();

		// then
		assertThat(all).hasSize(2);
	}

	@Test
	void shouldCreateSSHKey() {
		// given
		SSHKey request = SSHKey.builder().name("name").value("v").ownerId(new PersistentId("o1"))
				.createTime(LocalDateTime.now()).updateTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build();

		// when
		String newServiceId = repository.create(request);

		// then
		Optional<SSHKey> byId = repository.findById(newServiceId);
		assertThat(byId).isPresent();
		assertThat(byId.get().id).isNotNull();
		assertThat(byId.get().name).isEqualTo("name");
		assertThat(byId.get().sites).isEqualTo(request.sites);
	}

	@Test
	void shouldUpdateSSHKey() {
		// given
		SSHKeyEntity old = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());
		SSHKey requestToUpdate = SSHKey.builder().id(old.getId().toString()).name("name2").value("v2")
				.ownerId(new PersistentId("o1")).createTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString()))
				.updateTime(LocalDateTime.now()).build();

		// when
		repository.update(requestToUpdate);

		// then
		Optional<SSHKey> byId = repository.findById(old.getId().toString());
		assertThat(byId).isPresent();
		assertThat(byId.get().name).isEqualTo("name2");
		assertThat(byId.get().value).isEqualTo("v2");
		assertThat(byId.get().sites).isEqualTo(requestToUpdate.sites);
		assertThat(byId.get().ownerId.id).isEqualTo(old.getOwnerId());
	}

	@Test
	void savedSSHKeyExists() {
		// given
		SSHKeyEntity entity = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(repository.exists(entity.getId().toString())).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		// given
		String nonExistedId = generateId().toString();

		// when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists("")).isFalse();
	}

	@Test
	void shouldRemoveSSHKeySiteRefWhenAssociatedSiteHasRemoved() {
		// given
		SSHKeyEntity entity = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id.toString(), site2Id.toString())).build());

		// when
		siteRepository.delete(site1Id.toString());

		// then
		assertThat(repository.findById(entity.getId().toString()).get().sites).hasSize(1);
	}

	@Test
	void shouldReturnFalseForNonPresentName() {
		// given
		entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());
		String uniqueName = "unique_name";

		// when + then
		assertThat(repository.isNamePresent(uniqueName)).isFalse();
	}

	@Test
	void shouldReturnTrueIfNamePresent() {
		// given
		SSHKeyEntity existedKey = entityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(repository.isNamePresent(existedKey.getName())).isTrue();
	}

	@Test
	void shouldReturnTrueIfNameIsPresentOutOfSpecificRecord() {
		// given
		SSHKeyEntity existedKey = entityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());
		SSHKeyEntity existedKey2 = entityRepository.save(SSHKeyEntity.builder().name("name2").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(repository.isNamePresentIgnoringRecord(existedKey.getName(), existedKey2.getId().toString()))
				.isTrue();
	}

	@Test
	void shouldReturnFalseIfNameIsPresentOnlyInSpecificRecord() {
		// given
		SSHKeyEntity existedKey = entityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(repository.isNamePresentIgnoringRecord(existedKey.getName(), existedKey.getId().toString()))
				.isFalse();
	}

	@Test
	void shouldFailWhenCreateSSHKeyWithNotExistingSites() {
		// given
		SSHKey request = SSHKey.builder().name("name").value("v").ownerId(new PersistentId("o1"))
				.createTime(LocalDateTime.now()).updateTime(LocalDateTime.now())
				.sites(Sets.newSet(UUID.randomUUID().toString())).build();

		assertThrows(IllegalArgumentException.class, () -> repository.create(request));
	}

}
