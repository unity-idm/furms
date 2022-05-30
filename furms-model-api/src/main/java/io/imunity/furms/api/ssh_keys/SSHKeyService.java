/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteSSHKeys;

import java.util.Optional;
import java.util.Set;

public interface SSHKeyService {

	void assertIsEligibleToManageKeys();
	
	Optional<SSHKey> findById(SSHKeyId id);

	Set<SSHKey> findOwned();

	Set<SSHKey> findByOwnerId(PersistentId ownerId);

	SiteSSHKeys findSiteSSHKeysByUserIdAndSite(PersistentId userId, SiteId siteId);

	void create(SSHKey sshKey);

	void update(SSHKey sshKey);

	void delete(SSHKeyId id);

	boolean isNamePresent(String name);

	boolean isNamePresentIgnoringRecord(String name, SSHKeyId recordToIgnore);
}
