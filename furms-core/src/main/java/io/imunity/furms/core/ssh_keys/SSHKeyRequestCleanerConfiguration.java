/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class SSHKeyRequestCleanerConfiguration {

	public final Duration cleanStaleRequestsAfter;

	SSHKeyRequestCleanerConfiguration(
			@Value("${furms.sshkeys.cleanStaleRequestsAfter:1D}") Duration cleanStaleRequestsAfter) {

		this.cleanStaleRequestsAfter = cleanStaleRequestsAfter;
	}

}
