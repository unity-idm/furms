/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.utils.ValidationUtils.check;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.api.validation.exceptions.SSHKeyAuthzException;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@Component
public class SSHKeyServiceValidator {

	private final SSHKeyRepository sshKeysRepository;
	private final SiteRepository siteRepository;
	private final AuthzService authzService;

	SSHKeyServiceValidator(SSHKeyRepository sshKeysRepository, AuthzService authzService,
			SiteRepository siteRepository) {
		this.sshKeysRepository = sshKeysRepository;
		this.authzService = authzService;
		this.siteRepository = siteRepository;
	}

	void validateCreate(SSHKey sshKey) {
		notNull(sshKey, "SSH key object cannot be null.");
		validateName(sshKey.name);
		validateOwner(sshKey.ownerId);
		validateValue(sshKey.value);
		validateSites(sshKey);
	}

	void validateUpdate(SSHKey sshKey) {
		notNull(sshKey, "SSH key object cannot be null.");
		validateId(sshKey.id);
		validateIsNamePresentIgnoringRecord(sshKey.name, sshKey.id);
		validateOwner(sshKey.ownerId);
		validateValue(sshKey.value);
		validateSites(sshKey);
	}

	void validateDelete(String id) {
		validateId(id);
		validateOwner(sshKeysRepository.findById(id).get().ownerId);
	}

	void validateOwner(String ownerId) {
		notNull(ownerId, "SSH key owner id has to be declared.");
		check(authzService.getCurrentUserId().id.equals(ownerId), () -> new SSHKeyAuthzException(
				"SSH key owner id has to be equal to current manager id."));
	}

	private void validateId(String id) {
		notNull(id, "SSH key ID has to be declared.");
		check(sshKeysRepository.exists(id),
				() -> new IdNotFoundValidationError("SSH key with declared ID is not exists."));
	}

	void validateName(String name) {
		notNull(name, "SSHKey name has to be declared.");
		check(!sshKeysRepository.isNamePresent(name),
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
		check(!sshKeysRepository.isNamePresentIgnoringRecord(name, recordToIgnore),
				() -> new DuplicatedNameValidationError(
						"Invalid SSH key name: SSH key name has to be unique."));
	}

	void validateSites(SSHKey key) {
		if (key.sites == null) {
			return;
		}
		for (String site : key.sites) {
			check(siteRepository.exists(site),
					() -> new IllegalArgumentException("Incorrect Site ID: ID not exists in DB."));
		}

		if (!key.getKeyOptions().containsKey("from")) {

			Set<Site> sites = siteRepository.findAll().stream().filter(s -> key.sites.contains(s.getId())
					&& (s.isSshKeyFromOptionMandatory() != null && s.isSshKeyFromOptionMandatory()))
					.collect(Collectors.toSet());
			check(sites.isEmpty(),
					() -> new IllegalArgumentException("Incorrect Sites: "
							+ sites.stream().map(s -> s.getId())
									.collect(Collectors.joining(", "))
							+ " requires ssh key \"from\""));
		}
	}
}