/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_installation;

import static java.util.stream.StreamSupport.stream;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;

@Repository
class InstalledSSHKeyDatabaseRepository implements InstalledSSHKeyRepository {
	private final InstalledSSHKeyEntityRepository repository;

	InstalledSSHKeyDatabaseRepository(InstalledSSHKeyEntityRepository repository) {
		this.repository = repository;
	}

	@Override
	public List<InstalledSSHKey> findAll() {
		return stream(repository.findAll().spliterator(), false).map(entity -> InstalledSSHKey.builder()
				.id(entity.getId().toString()).siteId(entity.siteId.toString())
				.sshkeyId(entity.sshkeyId.toString()).value(entity.value).build())
				.collect(Collectors.toList());
	}

	@Override
	public String create(InstalledSSHKey installedSSHKey) {
		InstalledSSHKeyEntity installedSSHKeyEntity = InstalledSSHKeyEntity.builder()
				.siteId(UUID.fromString(installedSSHKey.siteId))
				.sshkeyId(UUID.fromString(installedSSHKey.sshkeyId)).value(installedSSHKey.value)
				.build();
		InstalledSSHKeyEntity saved = repository.save(installedSSHKeyEntity);
		return saved.getId().toString();
	}

	@Override
	public void update(String siteId, String keyId, String value) {
		repository.findBySshkeyIdAndSiteId(UUID.fromString(keyId), UUID.fromString(siteId))
				.map(entity -> InstalledSSHKeyEntity.builder().id(entity.getId()).siteId(entity.siteId)
						.sshkeyId(entity.sshkeyId).value(value).build())
				.ifPresent(repository::save);
	}

	@Override
	public void delete(String id) {
		repository.deleteById(UUID.fromString(id));
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		repository.deleteBySshkeyIdAndSiteId(UUID.fromString(sshkeyId), UUID.fromString(siteId));

	}
	
	@Override
	public void deleteBySSHKey(String sshkeyId) {
		repository.deleteBySshkeyId(UUID.fromString(sshkeyId));

	}

	@Override
	public List<InstalledSSHKey> findBySSHKeyId(String sshkeyId) {
		return stream(repository.findBySshkeyId(UUID.fromString(sshkeyId)).spliterator(), false)
				.map(entity -> InstalledSSHKey.builder().id(entity.getId().toString())
						.siteId(entity.siteId.toString()).sshkeyId(entity.sshkeyId.toString())
						.value(entity.value).build())
				.collect(Collectors.toList());
	}
}
