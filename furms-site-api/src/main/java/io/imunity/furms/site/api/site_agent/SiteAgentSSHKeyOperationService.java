/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyUpdating;

public interface SiteAgentSSHKeyOperationService {
	void addSSHKey(CorrelationId correlationId, SSHKeyAddition installation);
	void removeSSHKey(CorrelationId correlationId, SSHKeyRemoval deinstallation);
	void updateSSHKey(CorrelationId correlationId, SSHKeyUpdating updating);
}
