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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;

@Component
class SSHKeyRequestCleaner {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final SSHKeyOperationRepository sshKeyOperationRepository;

	private final SSHKeyRequestCleanerConfiguration configuration;
	private final TaskScheduler scheduler;

	SSHKeyRequestCleaner(SSHKeyOperationRepository sshKeyOperationRepository,
			SSHKeyRequestCleanerConfiguration configuration, TaskScheduler scheduler) {

		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.configuration = configuration;
		this.scheduler = scheduler;

		sheduleCleanStaleRequests();
	}

	private void sheduleCleanStaleRequests() {
		scheduler.scheduleWithFixedDelay(() -> cleanStaleRequest(),
				configuration.cleanStaleRequestsAfter.toMillis() / 10);
	}

	@Transactional
	private void cleanStaleRequest() {
		LOG.trace("Cleaning ssh key operation stale requests");
		List<SSHKeyOperationJob> findByStatus = sshKeyOperationRepository
				.findByStatus(SSHKeyOperationStatus.SEND);

				
		for (SSHKeyOperationJob job : findByStatus) {
			if (Duration.between(job.originationTime, LocalDateTime.now())
					.compareTo(configuration.cleanStaleRequestsAfter) >= 0) {
				LOG.info("SSH key operation ACK timeout for ssh key {}, changing status to {}",
						job.sshkeyId, SSHKeyOperationStatus.FAILED);
				sshKeyOperationRepository.update(job.id, SSHKeyOperationStatus.FAILED,
						Optional.of("SSH key operation ACK timeout"), LocalDateTime.now());
			}
		}
	}
}
