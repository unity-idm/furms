/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.db.ssh_key;

import static io.imunity.furms.utils.ValidationUtils.check;
import static java.util.Optional.empty;
import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@Repository
public class SSHKeyDatabaseRepository implements SSHKeyRepository {

	private final SSHKeyEntityRepository repository;
	private final SiteRepository siteRepository;

	SSHKeyDatabaseRepository(SSHKeyEntityRepository repository, SiteRepository siteRepository) {
		this.repository = repository;
		this.siteRepository = siteRepository;
	}

	@Override
	public String create(SSHKey sshKey) {
		validateKeyName(sshKey);
		validateKeyValue(sshKey);
		validateSites(sshKey);
		return repository
				.save(new SSHKeyEntity(sshKey.name, sshKey.value, sshKey.ownerId, sshKey.createTime,
						sshKey.updateTime,
						(sshKey.sites != null
								? sshKey.sites.stream()
										.map(s -> new SSHKeySiteReference(
												UUID.fromString(s)))
										.collect(Collectors.toSet())
								: Collections.emptySet())))
				.getId().toString();
	}

	@Override
	public Set<SSHKey> findAll() {
		return stream(repository.findAll().spliterator(), false).map(SSHKeyEntity::toSSHKey).collect(toSet());
	}

	@Override
	public Optional<SSHKey> findById(String id) {
		if (isEmpty(id)) {
			return empty();
		}
		return repository.findById(UUID.fromString(id)).map(SSHKeyEntity::toSSHKey);
	}

	@Override
	public Set<SSHKey> findAllByOwnerId(String ownerId) {
		if (isEmpty(ownerId)) {
			return Collections.emptySet();
		}
		return repository.findAllByOwnerId(ownerId).map(SSHKeyEntity::toSSHKey).collect(Collectors.toSet());
	}

	@Override
	public String update(SSHKey sshKey) {
		validateKeyId(sshKey);
		validateKeyName(sshKey);
		validateKeyValue(sshKey);
		validateSites(sshKey);

		return repository.findById(fromString(sshKey.id))
				.map(oldEntity -> SSHKeyEntity.builder().id(oldEntity.getId()).name(sshKey.name)
						.value(sshKey.value).createTime(sshKey.createTime)
						.updateTime(sshKey.updateTime).ownerId(sshKey.ownerId)
						.sites(sshKey.sites).build())
				.map(repository::save).map(SSHKeyEntity::getId).map(UUID::toString).get();
	}

	@Override
	public boolean exists(String id) {
		if (isEmpty(id)) {
			return false;
		}
		return repository.existsById(fromString(id));
	}

	@Override
	public boolean isNamePresent(String name) {
		return repository.existsByName(name);
	}

	@Override
	public boolean isNamePresentIgnoringRecord(String name, String recordToIgnore) {
		return repository.existsByNameAndIdIsNot(name, fromString(recordToIgnore));
	}

	@Override
	public void delete(String id) {
		check(!isEmpty(id), () -> new IllegalArgumentException("Incorrect delete SSH key input: ID is empty"));

		repository.deleteById(fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	private void validateKeyName(final SSHKey sshKey) {
		check(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		check(!isEmpty(sshKey.name),
				() -> new IllegalArgumentException("Incorrect SSH key name: name is empty"));
	}

	private void validateKeyId(final SSHKey sshKey) {
		check(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		check(!isEmpty(sshKey.id), () -> new IllegalArgumentException("Incorrect SSH key ID: ID is empty."));
		check(repository.existsById(fromString(sshKey.id)),
				() -> new IllegalArgumentException("Incorrect SSH key ID: ID not exists in DB."));
	}

	private void validateKeyValue(final SSHKey sshKey) {
		check(sshKey != null, () -> new IllegalArgumentException("SSH key object is missing."));
		check(!isEmpty(sshKey.value),
				() -> new IllegalArgumentException("Incorrect SSH key value: value is empty"));
	}

	private void validateSites(final SSHKey sshKey) {
		if (sshKey.sites == null) {
			return;
		}
		for (String site : sshKey.sites) {
			check(siteRepository.exists(site),
					() -> new IllegalArgumentException("Incorrect Site ID: ID not exists in DB."));
		}
	}

}
