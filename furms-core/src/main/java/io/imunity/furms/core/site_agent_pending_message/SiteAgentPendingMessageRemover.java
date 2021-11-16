/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.site_agent_pending_message;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Component;

@Component
class SiteAgentPendingMessageRemover {

	private final ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	private final ProjectOperationRepository projectOperationRepository;
	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final ResourceAccessRepository resourceAccessRepository;
	private final UserOperationRepository userOperationRepository;

	SiteAgentPendingMessageRemover(ProjectAllocationInstallationRepository projectAllocationInstallationRepository,
	                               ProjectOperationRepository projectOperationRepository, SSHKeyOperationRepository sshKeyOperationRepository, ResourceAccessRepository resourceAccessRepository, UserOperationRepository userOperationRepository) {
		this.projectAllocationInstallationRepository = projectAllocationInstallationRepository;
		this.projectOperationRepository = projectOperationRepository;
		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.resourceAccessRepository = resourceAccessRepository;
		this.userOperationRepository = userOperationRepository;
	}

	void remove(CorrelationId correlationId, String type){
		switch (type) {
			case "ProjectResourceAllocationRequest" :
			case "ProjectResourceDeallocationRequest" :
				projectAllocationInstallationRepository.delete(correlationId);
				break;
			case "ProjectInstallationRequest" :
			case "ProjectUpdateRequest" :
				projectOperationRepository.delete(correlationId);
				break;
			case "UserSSHKeyAddRequest" :
			case "UserSSHKeyRemovalRequest" :
			case "UserSSHKeyUpdateRequest" :
				sshKeyOperationRepository.delete(correlationId);
				break;
			case "UserAllocationGrantAccessRequest" :
			case "UserAllocationBlockAccessRequest" :
				resourceAccessRepository.delete(correlationId);
				break;
			case "UserProjectAddRequest" :
			case "UserProjectRemovalRequest" :
				userOperationRepository.delete(correlationId);
				break;
		}
	}
}
