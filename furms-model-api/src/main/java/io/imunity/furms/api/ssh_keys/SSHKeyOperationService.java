/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;

import java.util.List;

public interface SSHKeyOperationService {

	SSHKeyOperationJob findBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId);
	List<SSHKeyOperationJob> findBySSHKeyId(SSHKeyId sshkeyId);
	
}
