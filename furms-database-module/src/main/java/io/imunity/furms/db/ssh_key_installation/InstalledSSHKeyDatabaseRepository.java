/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_installation;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.StreamSupport.stream;

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
				.siteId(installedSSHKey.siteId.id)
				.sshkeyId(installedSSHKey.sshkeyId.id).value(installedSSHKey.value)
				.build();
		InstalledSSHKeyEntity saved = repository.save(installedSSHKeyEntity);
		return saved.getId().toString();
	}

	@Override
	public void update(SiteId siteId, SSHKeyId keyId, String value) {
		repository.findBySshkeyIdAndSiteId(keyId.id, siteId.id)
				.map(entity -> InstalledSSHKeyEntity.builder().id(entity.getId()).siteId(entity.siteId)
						.sshkeyId(entity.sshkeyId).value(value).build())
				.ifPresent(repository::save);
	}

	@Override
	public void delete(InstalledSSHKeyId id) {
		repository.deleteById(id.id);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public void deleteBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId) {
		repository.deleteBySshkeyIdAndSiteId(sshkeyId.id, siteId.id);

	}
	
	@Override
	public void deleteBySSHKey(SSHKeyId sshkeyId) {
		repository.deleteBySshkeyId(sshkeyId.id);

	}

	@Override
	public List<InstalledSSHKey> findBySSHKeyId(SSHKeyId sshkeyId) {
		return repository.findBySshkeyId(sshkeyId.id).stream()
				.map(entity -> InstalledSSHKey.builder().id(entity.getId().toString())
						.siteId(entity.siteId.toString()).sshkeyId(entity.sshkeyId.toString())
						.value(entity.value).build())
				.collect(Collectors.toList());
	}
}
