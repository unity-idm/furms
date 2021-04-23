/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.ssh_key_installation;

import java.util.List;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationJob;

public interface SSHKeyOperationService {
	
	void create(SSHKeyOperationJob installationJob);

	SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	void deleteBySSHKeyIdAndSiteId(String id, String id2);
	
	SSHKeyOperationJob findByCorrelationId(CorrelationId correlationId);

	List<SSHKeyOperationJob> findBySSHKey(String sshkeyId);
}
