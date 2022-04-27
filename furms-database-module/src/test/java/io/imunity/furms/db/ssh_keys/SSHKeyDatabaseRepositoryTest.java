/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import io.imunity.furms.db.DBIntegrationTest;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.imunity.furms.db.id.uuid.UUIDIdUtils.generateId;
import static java.time.Clock.systemUTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class SSHKeyDatabaseRepositoryTest extends DBIntegrationTest {

	@Autowired
	private SiteRepository siteRepository;

	@Autowired
	private SSHKeyDatabaseRepository repository;

	@Autowired
	private SSHKeyEntityRepository entityRepository;

	private SiteId site2Id;
	private SiteId site1Id;

	@BeforeEach
	void setUp() {
		entityRepository.deleteAll();
		siteRepository.deleteAll();
		site1Id = siteRepository.create(
				Site.builder().name("s1").connectionInfo("alala").build(), new SiteExternalId("id1"));
		site2Id = siteRepository.create(
				Site.builder().name("s2").connectionInfo("alala").build(), new SiteExternalId("id2"));
	}

	@Test
	void shouldFindCreatedSSHKey() {
		// given
		SSHKeyEntity entity = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now(systemUTC())).updateTime(LocalDateTime.now(systemUTC()))
				.sites(Sets.newSet(site1Id, site2Id)).build());

		// when
		Optional<SSHKey> byId = repository.findById(new SSHKeyId(entity.getId()));

		// then
		assertThat(byId).isPresent();
		SSHKey key = byId.get();
		assertThat(entity.getId()).isEqualTo(key.id.id);
		assertThat(entity.getOwnerId()).isEqualTo(key.ownerId.id);
		assertThat(entity.getName()).isEqualTo(key.name);
		assertThat(entity.getValue()).isEqualTo(key.value);
		assertThat(entity.getSites().stream().map(s -> new SiteId(s.getSiteId())).collect(Collectors.toSet()))
				.isEqualTo(key.sites);
	}

	@Test
	void shouldNotFindByIdIfDoesntExist() {
		// given
		SSHKeyId wrongId = new SSHKeyId(generateId());
		entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now()).build());

		// when
		Optional<SSHKey> byId = repository.findById(wrongId);

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
				.sites(Sets.newSet(site1Id, site2Id)).build();

		// when
		SSHKeyId newServiceId = repository.create(request);

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
				.sites(Sets.newSet(site1Id, site2Id))
				.updateTime(LocalDateTime.now()).build();

		// when
		repository.update(requestToUpdate);

		// then
		Optional<SSHKey> byId = repository.findById(new SSHKeyId(old.getId()));
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
		assertThat(repository.exists(new SSHKeyId(entity.getId()))).isTrue();
	}

	@Test
	void shouldNotExistsDueToEmptyOrWrongId() {
		// given
		SSHKeyId nonExistedId = new SSHKeyId(generateId());

		// when + then
		assertThat(repository.exists(nonExistedId)).isFalse();
		assertThat(repository.exists(null)).isFalse();
		assertThat(repository.exists(new SSHKeyId((UUID) null))).isFalse();
	}

	@Test
	void shouldRemoveSSHKeySiteRefWhenAssociatedSiteHasRemoved() {
		// given
		SSHKeyEntity entity = entityRepository.save(SSHKeyEntity.builder().name("name").value("v").ownerId("o1")
				.createTime(LocalDateTime.now())
				.sites(Sets.newSet(site1Id, site2Id)).build());

		// when
		siteRepository.delete(site1Id);

		// then
		assertThat(repository.findById(new SSHKeyId(entity.getId())).get().sites).hasSize(1);
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
		assertThat(repository.isNamePresentIgnoringRecord(existedKey.getName(), new SSHKeyId(existedKey2.getId())))
			.isTrue();
	}

	@Test
	void shouldReturnFalseIfNameIsPresentOnlyInSpecificRecord() {
		// given
		SSHKeyEntity existedKey = entityRepository.save(SSHKeyEntity.builder().name("name").value("v")
				.ownerId("o1").createTime(LocalDateTime.now()).build());

		// when + then
		assertThat(repository.isNamePresentIgnoringRecord(existedKey.getName(), new SSHKeyId(existedKey.getId())))
				.isFalse();
	}

	@Test
	void shouldFailWhenCreateSSHKeyWithNotExistingSites() {
		// given
		SSHKey request = SSHKey.builder().name("name").value("v").ownerId(new PersistentId("o1"))
				.createTime(LocalDateTime.now()).updateTime(LocalDateTime.now())
				.sites(Sets.newSet(new SiteId(UUID.randomUUID()))).build();

		assertThrows(IllegalArgumentException.class, () -> repository.create(request));
	}

}
