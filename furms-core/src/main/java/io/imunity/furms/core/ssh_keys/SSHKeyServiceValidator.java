/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyAuthzException;
import io.imunity.furms.api.ssh_keys.SSHKeyHistoryException;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Component
public class SSHKeyServiceValidator {

	private final SSHKeyRepository sshKeysRepository;
	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SSHKeyHistoryRepository sshKeyHistoryRepository;
	private final SiteRepository siteRepository;
	private final UsersDAO userDao;
	private final AuthzService authzService;

	SSHKeyServiceValidator(SSHKeyRepository sshKeysRepository, AuthzService authzService,
			SiteRepository siteRepository, SSHKeyOperationRepository sshKeyOperationRepository,
			UsersDAO userDao, SSHKeyHistoryRepository sshKeyHistoryRepository) {
		this.sshKeysRepository = sshKeysRepository;
		this.authzService = authzService;
		this.siteRepository = siteRepository;
		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.userDao = userDao;
		this.sshKeyHistoryRepository = sshKeyHistoryRepository;
	}

	void validateCreate(SSHKey sshKey) {
		notNull(sshKey, "SSH key object cannot be null.");
		validateName(sshKey.name);
		validateOwner(sshKey.ownerId);
		validateFenixId(sshKey.ownerId);
		validateValue(sshKey.value);
		validateSites(sshKey);
	}

	void validateUpdate(SSHKey sshKey) {
		notNull(sshKey, "SSH key object cannot be null.");
		validateId(sshKey.id);
		validateIsNamePresentIgnoringRecord(sshKey.name, sshKey.id);
		validateOwner(sshKey.ownerId);
		validateFenixId(sshKey.ownerId);
		validateValue(sshKey.value);
		validateSites(sshKey);
		validateOpenOperation(sshKey);
	}

	void validateDelete(String id) {
		validateId(id);

		final SSHKey findById = sshKeysRepository.findById(id)
				.orElseThrow(() -> new IllegalStateException("SSH Key not found: " + id));
		validateOwner(findById.ownerId);
		validateFenixId(findById.ownerId);
		validateOpenOperation(findById);
	}

	void validateOwner(PersistentId ownerId) {
		notNull(ownerId, "SSH key owner id has to be declared.");
		assertTrue(authzService.getCurrentUserId().equals(ownerId), () -> new SSHKeyAuthzException(
				"SSH key owner id has to be equal to current manager id."));
	}
	
	void validateFenixId(PersistentId ownerId)
	{
		assertTrue(userDao.findById(ownerId).get().fenixUserId.isPresent(),
				() -> new UserWithoutFenixIdValidationError("User not logged via Fenix Central IdP"));
	}

	private void validateId(String id) {
		notNull(id, "SSH key ID has to be declared.");
		assertTrue(sshKeysRepository.exists(id),
				() -> new IdNotFoundValidationError("SSH key with declared ID is not exists."));
	}

	void validateName(String name) {
		notNull(name, "SSHKey name has to be declared.");
		assertTrue(!sshKeysRepository.isNamePresent(name),
				() -> new DuplicatedNameValidationError("SSHKey name has to be unique."));
	}

	void validateValue(String value) {
		notNull(value, "SSH key value has to be declared.");
		hasText(value, "Invalid SSH key value: SSH key value is empty.");
		SSHKey.validate(value);
	}

	void validateIsNamePresentIgnoringRecord(String name, String recordToIgnore) {
		notNull(recordToIgnore, "SSH key id has to be declared.");
		notNull(name, "Invalid SSH key name: SSH key name is empty.");
		assertTrue(!sshKeysRepository.isNamePresentIgnoringRecord(name, recordToIgnore),
				() -> new DuplicatedNameValidationError(
						"Invalid SSH key name: SSH key name has to be unique."));
	}

	void validateSites(SSHKey key) {
		if (key.sites == null) {
			return;
		}
		for (String site : key.sites) {
			assertTrue(siteRepository.exists(site),
					() -> new IllegalArgumentException("Incorrect Site ID: ID not exists in DB."));
		}

		if (!key.getKeyOptions().containsKey("from")) {

			Set<Site> sites = siteRepository.findAll().stream().filter(s -> key.sites.contains(s.getId())
					&& (s.isSshKeyFromOptionMandatory() != null && s.isSshKeyFromOptionMandatory()))
					.collect(Collectors.toSet());
			assertTrue(sites.isEmpty(),
					() -> new IllegalArgumentException("Incorrect Sites: "
							+ sites.stream().map(s -> s.getId())
									.collect(Collectors.joining(", "))
							+ " requires ssh key \"from\""));
		}
	}

	void validateOpenOperation(SSHKey key) {
		assertTrue(sshKeyOperationRepository.findBySSHKey(key.id).stream()
				.filter(operation -> operation.status.inProgress()).findAny().isEmpty(),
				() -> new IllegalArgumentException(
						"Invalid SSH key: there are uncompleted key operations"));
	}


	void assertKeyWasNotUsedPreviously(Site site, SSHKey sshKey) {
		if (!site.isSshKeyHistoryActive())
			return;
		String fingerprint = sshKey.getFingerprint();	
		assertTrue(sshKeyHistoryRepository.findBySiteIdAndOwnerIdLimitTo(site.getId(), sshKey.ownerId.id, site.getSshKeyHistoryLength())
				.stream().filter(h -> h.sshkeyFingerprint.equals(fingerprint)).findAny().isEmpty(),
				() -> new SSHKeyHistoryException("Invalid SSH key: the key does not meet the history requirements", site.getId()));
	}
	
	void assertIsEligibleToManageKeys() {
		validateFenixId(authzService.getCurrentAuthNUser().id.get());
	}
}



