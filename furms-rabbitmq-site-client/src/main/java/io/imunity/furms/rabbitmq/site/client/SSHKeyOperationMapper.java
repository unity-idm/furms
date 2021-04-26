/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingRequest;
import io.imunity.furms.site.api.ssh_keys.SSHKeyAddition;
import io.imunity.furms.site.api.ssh_keys.SSHKeyRemoval;
import io.imunity.furms.site.api.ssh_keys.SSHKeyUpdating;

class SSHKeyOperationMapper
{
	static AgentSSHKeyAdditionRequest map(SSHKeyAddition installation)
	{
		return AgentSSHKeyAdditionRequest.builder().fenixUserId(installation.user.id)
				.publicKey(installation.publicKey).uid(installation.userUid).build();
	}

	static AgentSSHKeyRemovalRequest map(SSHKeyRemoval deInstallation)
	{
		return AgentSSHKeyRemovalRequest.builder().fenixUserId(deInstallation.user.id)
				.publicKey(deInstallation.publicKey).uid(deInstallation.userUid).build();
	}

	static AgentSSHKeyUpdatingRequest map(SSHKeyUpdating updating)
	{
		return AgentSSHKeyUpdatingRequest.builder().fenixUserId(updating.user.id)
				.oldPublicKey(updating.oldPublicKey).newPublicKey(updating.newPublicKey)
				.uid(updating.userUid).build();
	}
}
