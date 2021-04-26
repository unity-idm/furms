/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;

public interface SSHKeyOperationService {
	
	void create(SSHKeyOperationJob installationJob);

	SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	void deleteBySSHKeyIdAndSiteId(String id, String id2);

	List<SSHKeyOperationJob> findBySSHKey(String sshkeyId);
}
