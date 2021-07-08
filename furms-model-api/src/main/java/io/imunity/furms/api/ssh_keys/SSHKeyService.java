/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.ssh_keys.SSHKey;

public interface SSHKeyService {

	void assertIsEligibleToManageKeys();
	
	Optional<SSHKey> findById(String id);

	Set<SSHKey> findOwned();

	Set<SSHKey> findByOwnerId(String ownerId);

	String create(SSHKey sshKey);

	String update(SSHKey sshKey);

	void delete(String id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);
}
