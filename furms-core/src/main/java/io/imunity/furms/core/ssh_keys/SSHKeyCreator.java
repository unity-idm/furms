/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@Component
public class SSHKeyCreator {

	private static final Logger LOG = LoggerFactory.getLogger(SSHKeyCreator.class);

	private final SSHKeyRepository sshKeysRepository;

	SSHKeyCreator(SSHKeyRepository sshKeysRepository) {
		super();
		this.sshKeysRepository = sshKeysRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public SSHKey create(SSHKey sshKey) {
		String created = sshKeysRepository.create(sshKey);
		SSHKey createdKey = sshKeysRepository.findById(created).orElseThrow(
				() -> new IllegalStateException("SSH key has not been saved to DB correctly."));
		LOG.info("Created SSHKey in repository: {}", createdKey);
		return createdKey;
	}
}
