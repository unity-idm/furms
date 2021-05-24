/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_installation;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;

public interface InstalledSSHKeyRepository {

	String create(InstalledSSHKey installedSSHKey);

	List<InstalledSSHKey> findAll();

	List<InstalledSSHKey> findBySSHKeyId(String sshkeyId);

	void delete(String id);

	void deleteAll();

	void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId);
	
	void deleteBySSHKey(String sshkeyId);

	void update(String siteId, String id, String value);
}
