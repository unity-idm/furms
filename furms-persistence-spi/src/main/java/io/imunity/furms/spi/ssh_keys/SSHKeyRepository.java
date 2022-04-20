/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.spi.ssh_keys;

import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.users.PersistentId;

import java.util.Optional;
import java.util.Set;

public interface SSHKeyRepository {

	Optional<SSHKey> findById(SSHKeyId id);
	
	Set<SSHKey> findAll();
	
	Set<SSHKey> findAllByOwnerId(PersistentId ownerId);

	String create(SSHKey sshKey);

	String update(SSHKey sshKey);

	boolean exists(SSHKeyId id);

	void delete(SSHKeyId id);

	void deleteAll();

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);
}
