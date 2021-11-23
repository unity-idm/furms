/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.AgentSSHKeyRemover;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import org.springframework.stereotype.Component;

@Component
class PendingSSHKeyOperationRemover implements AgentSSHKeyRemover {
	private final SSHKeyOperationRepository sshKeyOperationRepository;

	PendingSSHKeyOperationRemover(SSHKeyOperationRepository sshKeyOperationRepository) {
		this.sshKeyOperationRepository = sshKeyOperationRepository;
	}

	@Override
	public void remove(CorrelationId correlationId) {
		sshKeyOperationRepository.delete(correlationId);
	}
}
