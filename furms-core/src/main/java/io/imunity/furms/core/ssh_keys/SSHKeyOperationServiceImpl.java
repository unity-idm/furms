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

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
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
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;
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

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Transactional
	@Override
	public void create(SSHKeyOperationJob installationJob) {
		sshKeyOperationRepository.create(installationJob);
		LOG.info("SSHKeyInstallationJob was created: {}", installationJob);

	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Transactional
	@Override
	public void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(sshkeyId, siteId);
		LOG.info("SSHKeyInstallationJob for key={} and site={} was deleted", sshkeyId, siteId);

	}
	
	@Override
	public List<SSHKeyOperationJob> findBySSHKey(String sshkeyId) {
		return sshKeyOperationRepository.findBySSHKey(sshkeyId);

	}

	// FIXME To auth this method special user for queue message resolving is
	// needed
	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, SSHKeyOperationStatus status, Optional<String> error) {
		SSHKeyOperationJob job = sshKeyOperationRepository.findByCorrelationId(correlationId);
		sshKeyOperationRepository.update(job.id, status, error, LocalDateTime.now());
		LOG.info("SSHKeyOperationJob status with given id {} was update to {}", job.id, status);
		if (status.equals(DONE) && job.operation.equals(ADD))
		{
			addKeyHistory(job.siteId, job.sshkeyId);
		}
		
	}

	private void addKeyHistory(String siteId, String sshkeyId) {
		Optional<SSHKey> findById = sshKeysRepository.findById(sshkeyId);
		sshKeyHistoryRepository.create(SSHKeyHistory.builder().siteId(siteId).originationTime(LocalDateTime.now())
				.sshkeyFingerprint(findById.get().getFingerprint()).build());
		sshKeyHistoryRepository.deleteOldestLeaveOnly(siteId, MAX_HISTORY_SIZE);
	}

	// FIXME To auth this method special user for queue message resolving is
	// needed
	@Override
	@Transactional
	public void onSSHKeyRemovalFromSite(CorrelationId correlationId) {
		removeSSHKeyIfRemovedFromLastSite(correlationId);

	}

	private void removeSSHKeyIfRemovedFromLastSite(CorrelationId correlationId) {
		SSHKeyOperationJob operationJob = sshKeyOperationRepository.findByCorrelationId(correlationId);
		List<SSHKeyOperationJob> keysOperations = sshKeyOperationRepository.findBySSHKey(operationJob.sshkeyId);
		if (!keysOperations.stream().filter(o -> !o.operation.equals(REMOVE) || !o.status.equals(DONE))
				.findAny().isPresent()) {
			for (SSHKeyOperationJob job : keysOperations) {
				sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(job.sshkeyId, job.siteId);
			}

			sshKeysRepository.delete(operationJob.sshkeyId);
			LOG.info("Removed SSH key from repository with ID={}", operationJob.sshkeyId);
		}
	}

}
