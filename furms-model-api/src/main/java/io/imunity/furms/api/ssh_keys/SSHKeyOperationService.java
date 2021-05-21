/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;

public interface SSHKeyOperationService {

	SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId);
	List<SSHKeyOperationJob> findBySSHKeyId(String sshkeyId);
	
}
