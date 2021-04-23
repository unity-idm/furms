/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyUpdating;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingRequest;

class SSHKeyOperationMapper {

	// TODO ADD UID
	static AgentSSHKeyAdditionRequest map(SSHKeyAddition installation) {
		return AgentSSHKeyAdditionRequest.builder().fenixUserId(installation.user.id)
				.publicKey(installation.publicKey).uid("uid").build();
	}

	// TODO ADD UID
	static AgentSSHKeyRemovalRequest map(SSHKeyRemoval deInstallation) {
		return AgentSSHKeyRemovalRequest.builder().fenixUserId(deInstallation.user.id)
				.publicKey(deInstallation.publicKey).uid("uid").build();
	}

	// TODO ADD UID
	static AgentSSHKeyUpdatingRequest map(SSHKeyUpdating updating) {
		return AgentSSHKeyUpdatingRequest.builder().fenixUserId(updating.user.id)
				.oldPublicKey(updating.oldPublicKey).newPublicKey(updating.newPublicKey).uid("uid")
				.build();
	}

}
