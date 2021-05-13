/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.constant.SSHKeysConst.MAX_HISTORY_SIZE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.REMOVE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.FAILED;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.api.ssh_keys.SSHKeyOperationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationResult;
import io.imunity.furms.site.api.message_resolver.SSHKeyOperationMessageResolver;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;

@Service
class SSHKeyOperationServiceImpl implements SSHKeyOperationService, SSHKeyOperationMessageResolver {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SSHKeyRepository sshKeysRepository;
	private final SSHKeyHistoryRepository sshKeyHistoryRepository;

	SSHKeyOperationServiceImpl(SSHKeyOperationRepository sshKeyInstallationRepository,
			SSHKeyRepository sshKeysRepository, SSHKeyHistoryRepository sshKeyHistoryRepository) {
		this.sshKeyOperationRepository = sshKeyInstallationRepository;
		this.sshKeysRepository = sshKeysRepository;
		this.sshKeyHistoryRepository = sshKeyHistoryRepository;
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Override
	public SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		return sshKeyOperationRepository.findBySSHKeyIdAndSiteId(sshkeyId, siteId);
	}

	// FIXME To auth this method special user for queue message resolving is
	// needed
	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, SSHKeyOperationResult result) {
		SSHKeyOperationJob job = sshKeyOperationRepository.findByCorrelationId(correlationId);
		if (job.status.equals(FAILED) || job.status.equals(DONE)) {
			LOG.info("SSHKeyOperation with given correlation id {} cannot be modified", correlationId.id);
			return;
		}

		sshKeyOperationRepository.update(job.id, result.status, Optional.ofNullable(result.error.message),
				LocalDateTime.now());
		LOG.info("SSHKeyOperationJob status with given id {} was update to {}", job.id, result.status);

		if (result.status.equals(DONE)) {
			if (job.operation.equals(ADD)) {
				addKeyHistory(job.siteId, job.sshkeyId);
			} else if (job.operation.equals(REMOVE)) {
				removeSSHKeyIfRemovedFromLastSite(job);
			}
		}

	}

	private void addKeyHistory(String siteId, String sshkeyId) {
		Optional<SSHKey> findById = sshKeysRepository.findById(sshkeyId);
		sshKeyHistoryRepository.create(SSHKeyHistory.builder().siteId(siteId)
				.originationTime(LocalDateTime.now(ZoneOffset.UTC))
				.sshkeyFingerprint(findById.get().getFingerprint())
				.sshkeyOwnerId(findById.get().ownerId).build());
		sshKeyHistoryRepository.deleteOldestLeaveOnly(siteId, findById.get().ownerId.id, MAX_HISTORY_SIZE);
	}

	private void removeSSHKeyIfRemovedFromLastSite(SSHKeyOperationJob operationJob) {
		List<SSHKeyOperationJob> keysOperations = sshKeyOperationRepository.findBySSHKey(operationJob.sshkeyId);
		if (!keysOperations.stream().filter(o -> !o.operation.equals(REMOVE) || !o.status.equals(DONE))
				.findAny().isPresent()) {
			for (SSHKeyOperationJob job : keysOperations) {
				sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(job.sshkeyId, job.siteId);
			}
			Optional<SSHKey> removed = sshKeysRepository.findById(operationJob.sshkeyId);
			sshKeysRepository.delete(operationJob.sshkeyId);
			LOG.info("Removed SSH key from repository with ID={}, {}", operationJob.sshkeyId, removed.orElse(null));
		}
	}

}
