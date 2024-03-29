/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

@Repository
class SSHKeyDatabaseRepository implements SSHKeyRepository {

	private final SSHKeyEntityRepository repository;
	private final SiteRepository siteRepository;

	SSHKeyDatabaseRepository(SSHKeyEntityRepository repository, SiteRepository siteRepository) {
		this.repository = repository;
		this.siteRepository = siteRepository;
	}

	@Override
	public SSHKeyId create(SSHKey sshKey) {
		validateKeyName(sshKey);
		validateKeyValue(sshKey);
		validateSites(sshKey);
		SSHKeyEntity saved = repository
			.save(new SSHKeyEntity(sshKey.name, sshKey.value, sshKey.ownerId.id, sshKey.createTime,
				sshKey.updateTime,
				(sshKey.sites != null
					? sshKey.sites.stream()
					.map(siteId -> new SSHKeySiteReference(siteId.id))
					.collect(toSet())
					: Collections.emptySet())));
		return new SSHKeyId(saved.getId());
	}

	@Override
	public Set<SSHKey> findAll() {
		return stream(repository.findAll().spliterator(), false).map(SSHKeyEntity::toSSHKey).collect(toSet());
	}

	@Override
	public Optional<SSHKey> findById(SSHKeyId id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(id.id).map(SSHKeyEntity::toSSHKey);
	}

	@Override
	public Set<SSHKey> findAllByOwnerId(PersistentId ownerId) {
		if (ObjectUtils.isEmpty(ownerId)) {
			return Collections.emptySet();
		}
		return repository.findAllByOwnerId(ownerId.id).stream()
				.map(SSHKeyEntity::toSSHKey)
				.collect(toSet());
	}

	@Override
	public SSHKeyId update(SSHKey sshKey) {
		validateKeyId(sshKey);
		validateKeyName(sshKey);
		validateKeyValue(sshKey);
		validateSites(sshKey);

		return repository.findById(sshKey.id.id)
				.map(oldEntity -> SSHKeyEntity.builder().id(oldEntity.getId()).name(sshKey.name)
						.value(sshKey.value).createTime(sshKey.createTime)
						.updateTime(sshKey.updateTime).ownerId(sshKey.ownerId.id)
						.sites(sshKey.sites).build())
				.map(repository::save)
				.map(SSHKeyEntity::getId)
				.map(SSHKeyId::new)
				.orElseThrow(() -> new IllegalStateException("SSH Key not found: " + sshKey));
	}

	@Override
	public boolean exists(SSHKeyId id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(id.id);
	}

	@Override
	public boolean isNamePresent(String name) {
		return repository.existsByName(name);
	}

	@Override
	public boolean isNamePresentIgnoringRecord(String name, SSHKeyId recordToIgnore) {
		return repository.existsByNameAndIdIsNot(name, recordToIgnore.id);
	}

	@Override
	public void delete(SSHKeyId id) {
		assertTrue(!isEmpty(id), () -> new IllegalArgumentException("Incorrect delete SSH key input: ID is empty"));

		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private void validateKeyName(final SSHKey sshKey) {
		assertTrue(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		assertTrue(!ObjectUtils.isEmpty(sshKey.name),
				() -> new IllegalArgumentException("Incorrect SSH key name: name is empty"));
	}

	private void validateKeyId(final SSHKey sshKey) {
		assertTrue(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		assertTrue(!ObjectUtils.isEmpty(sshKey.id), () -> new IllegalArgumentException("Incorrect SSH key ID: ID is empty."));
		assertTrue(repository.existsById(sshKey.id.id),
				() -> new IllegalArgumentException("Incorrect SSH key ID: ID not exists in DB."));
	}

	private void validateKeyValue(final SSHKey sshKey) {
		assertTrue(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		assertTrue(!ObjectUtils.isEmpty(sshKey.value),
				() -> new IllegalArgumentException("Incorrect SSH key value: value is empty"));
	}

	private void validateSites(final SSHKey sshKey) {
		if (sshKey.sites == null) {
			return;
		}
		for (SiteId site : sshKey.sites) {
			assertTrue(siteRepository.exists(site),
					() -> new IllegalArgumentException("Incorrect Site ID: ID not exists in DB."));
		}
	}

	private boolean isEmpty(SSHKeyId id) {
		return id == null || id.id == null;
	}
}
