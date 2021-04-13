/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.spi.ssh_keys;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.ssh_key.SSHKey;
import io.imunity.furms.domain.users.PersistentId;

public interface SSHKeyRepository {

	Optional<SSHKey> findById(String id);
	
	Set<SSHKey> findAll();
	
	Set<SSHKey> findAllByOwnerId(PersistentId ownerId);

	String create(SSHKey sshKey);

	String update(SSHKey sshKey);

	boolean exists(String id);

	void delete(String id);

	void deleteAll();

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);
}
