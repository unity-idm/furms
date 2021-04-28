/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.spi.ssh_key_installation.SSHKeyOperationRepository;

@Component
class SSHKeyRequestCleaner {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final SSHKeyOperationRepository sshKeyOperationRepository;

	@Value("${furms.sshkeys.cleanStaleRequestsAfter}")
	private int cleanStaleRequestsAfter;

	SSHKeyRequestCleaner(SSHKeyOperationRepository sshKeyOperationRepository) {

		this.sshKeyOperationRepository = sshKeyOperationRepository;
	}

	@Scheduled(fixedDelayString = "${furms.sshkeys.cleanStaleRequestsAfter:86400000}")
	@Transactional
	private void cleanStaleRequest() {
		LOG.debug("Cleaning ssh key operation stale requests");
		List<SSHKeyOperationJob> findByStatus = sshKeyOperationRepository
				.findByStatus(SSHKeyOperationStatus.SEND);

		for (SSHKeyOperationJob job : findByStatus) {
			if (Duration.between(job.operationTime, LocalDateTime.now())
					.toMillis() >= cleanStaleRequestsAfter) {
				LOG.info("SSH key operation ACK timeout for ssh key {}, changing status to {}",
						job.sshkeyId, SSHKeyOperationStatus.FAILED);
				sshKeyOperationRepository.update(job.id, SSHKeyOperationStatus.FAILED,
						Optional.of("SSH key operation ACK timeout"), LocalDateTime.now());
			}
		}
	}
}
