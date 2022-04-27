/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_installation;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKey;
import io.imunity.furms.domain.ssh_keys.InstalledSSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;

import java.util.List;

public interface InstalledSSHKeyRepository {

	InstalledSSHKeyId create(InstalledSSHKey installedSSHKey);

	List<InstalledSSHKey> findAll();

	List<InstalledSSHKey> findBySSHKeyId(SSHKeyId sshkeyId);

	void delete(InstalledSSHKeyId id);

	void deleteAll();

	void deleteBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId);
	
	void deleteBySSHKey(SSHKeyId sshkeyId);

	void update(SiteId siteId, SSHKeyId id, String value);
}
