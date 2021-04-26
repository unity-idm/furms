/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import java.util.Optional;
import java.util.Set;

import io.imunity.furms.domain.ssh_keys.SSHKey;

public interface SSHKeyService {

	Optional<SSHKey> findById(String id);

	Set<SSHKey> findOwned();

	String create(SSHKey sshKey);

	String update(SSHKey sshKey);

	void delete(String id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, String recordToIgnore);
}
