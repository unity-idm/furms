/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.utils.ValidationUtils.check;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@Service
class SSHKeyServiceImpl implements SSHKeyService {

	private static final Logger LOG = LoggerFactory.getLogger(SSHKeyServiceImpl.class);

	private final SSHKeyRepository sshKeysRepository;
	private final SSHKeyServiceValidator validator;
	private final AuthzService authzService;

	SSHKeyServiceImpl(SSHKeyRepository sshKeysRepository, SSHKeyServiceValidator validator,
			AuthzService authzService) {
		this.validator = validator;
		this.authzService = authzService;
		this.sshKeysRepository = sshKeysRepository;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public Optional<SSHKey> findById(String id) {
		LOG.debug("Getting SSH key with id={}", id);
		Optional<SSHKey> key = sshKeysRepository.findById(id);
		if (!key.isEmpty()) {
			validator.validateOwner(key.get().ownerId);
		}
		return key;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public Set<SSHKey> findOwned() {
		String ownerId = authzService.getCurrentUserId().id;
		LOG.debug("Getting all SSH keys for owner {}", ownerId);
		return sshKeysRepository.findAllByOwnerId(ownerId);
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public String create(SSHKey sshKey) {
		validator.validateCreate(sshKey);
		String created = sshKeysRepository.create(sshKey);
		SSHKey createdKey = sshKeysRepository.findById(created).orElseThrow(
				() -> new IllegalStateException("SSH key has not been saved to DB correctly."));
		LOG.info("Created SSHKey in repository: {}", createdKey);
		return created;
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public String update(SSHKey sshKey) {
		validator.validateUpdate(sshKey);
		SSHKey merged = merge(sshKeysRepository.findById(sshKey.id).get(), sshKey);
		String updatedId = sshKeysRepository.update(merged);
		LOG.info("Update SSH key in repository with ID={}, {}", sshKey.id, merged);
		return updatedId;
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public void delete(String id) {
		validator.validateDelete(id);
		sshKeysRepository.delete(id);
		LOG.info("Removed SSH key from repository with ID={}", id);
	}

	private SSHKey merge(SSHKey oldKey, SSHKey key) {
		check(oldKey.id.equals(key.id),
				() -> new IllegalArgumentException("There are different SSH key during merge"));
		return SSHKey.builder().id(oldKey.id).name(key.name).value(ofNullable(key.value).orElse(oldKey.value))
				.ownerId(ofNullable(key.ownerId).orElse(oldKey.ownerId)).createTime(oldKey.createTime)
				.updateTime(key.updateTime).sites(ofNullable(key.sites).orElse(oldKey.sites)).build();
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public boolean isNamePresent(String name) {
		try {
			validator.validateName(name);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public boolean isNamePresentIgnoringRecord(String name, String recordToIgnore) {
		try {
			validator.validateIsNamePresentIgnoringRecord(name, recordToIgnore);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

}
