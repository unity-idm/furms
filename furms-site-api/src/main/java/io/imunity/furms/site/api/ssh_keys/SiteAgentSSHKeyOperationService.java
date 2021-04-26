/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.ssh_keys;

import io.imunity.furms.domain.site_agent.CorrelationId;

public interface SiteAgentSSHKeyOperationService {
	void addSSHKey(CorrelationId correlationId, SSHKeyAddition installation);
	void removeSSHKey(CorrelationId correlationId, SSHKeyRemoval deinstallation);
	void updateSSHKey(CorrelationId correlationId, SSHKeyUpdating updating);
}
