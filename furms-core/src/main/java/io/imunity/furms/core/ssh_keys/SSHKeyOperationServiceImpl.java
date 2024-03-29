/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.api.ssh_keys.SSHKeyOperationService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.InvalidCorrelationIdException;
import io.imunity.furms.domain.site_agent.IllegalStateTransitionException;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.*;
import io.imunity.furms.site.api.status_updater.SSHKeyOperationStatusUpdater;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.constant.SSHKeysConst.MAX_HISTORY_SIZE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.*;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.FAILED;

@Service
class SSHKeyOperationServiceImpl implements SSHKeyOperationService, SSHKeyOperationStatusUpdater {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SSHKeyRepository sshKeysRepository;
	private final SSHKeyHistoryRepository sshKeyHistoryRepository;
	private final InstalledSSHKeyRepository installedSSHKeyRepository;

	SSHKeyOperationServiceImpl(SSHKeyOperationRepository sshKeyInstallationRepository,
			SSHKeyRepository sshKeysRepository, SSHKeyHistoryRepository sshKeyHistoryRepository,
			InstalledSSHKeyRepository installedSSHKeyRepository) {
		this.sshKeyOperationRepository = sshKeyInstallationRepository;
		this.sshKeysRepository = sshKeysRepository;
		this.sshKeyHistoryRepository = sshKeyHistoryRepository;
		this.installedSSHKeyRepository = installedSSHKeyRepository;
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	@Override
	public SSHKeyOperationJob findBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId) {
		return sshKeyOperationRepository.findBySSHKeyIdAndSiteId(sshkeyId, siteId);
	}
	
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	@Override
	public List<SSHKeyOperationJob> findBySSHKeyId(SSHKeyId sshkeyId) {
		return sshKeyOperationRepository.findBySSHKey(sshkeyId);
	}
	
	
	// FIXME To auth this method special user for queue message resolving is
	// needed
	@Override
	@Transactional
	public void updateStatus(CorrelationId correlationId, SSHKeyOperationResult result) {
		SSHKeyOperationJob job = sshKeyOperationRepository.findByCorrelationId(correlationId);
		if (job == null) {
			throw new InvalidCorrelationIdException("SSHKeyOperation does not exists");
		}
		
		if (job.status.equals(FAILED) || job.status.equals(DONE)) {
			throw new IllegalStateTransitionException(String.format("SSHKeyOperation status is %s, it cannot be modified", job.status));
		}

		sshKeyOperationRepository.update(job.id, result.status, Optional.ofNullable(result.error.message),
				LocalDateTime.now());
		LOG.info("SSHKeyOperationJob status with given id {} was update to {}", job.id, result.status);

		if (result.status.equals(DONE)) {
			SSHKey key = sshKeysRepository.findById(job.sshkeyId).get();
			if (job.operation.equals(ADD)) {
				addKeyHistory(job.siteId, key);
				addToInstalledKeys(job.siteId, key);
			} else if (job.operation.equals(REMOVE)) {
				removeFromInstalledKeys(job.siteId, key);
				removeSSHKeyIfRemovedFromLastSite(job);
			}else if (job.operation.equals(UPDATE)) {
				
				List<SSHKeyHistory> history = sshKeyHistoryRepository
						.findBySiteIdAndOwnerIdLimitTo(job.siteId, key.ownerId.id, 1);
				if (history.size() > 0
						&& !history.get(0).sshkeyFingerprint.equals(key.getFingerprint())) {
					addKeyHistory(job.siteId, key);
				}
				updateInstalledKeys(job.siteId, key);
			}
		}

	}

	private void addToInstalledKeys(SiteId siteId, SSHKey key) {
		LOG.debug("Add SSH key {} to installed keys", key);
		installedSSHKeyRepository.deleteBySSHKeyIdAndSiteId(key.id, siteId);
		installedSSHKeyRepository.create(InstalledSSHKey.builder().siteId(siteId)
				.value(key.value).sshkeyId(key.id).build());
	}
	
	private void updateInstalledKeys(SiteId siteId, SSHKey key) {
		LOG.debug("Update SSH key {} in installed keys", key);
		installedSSHKeyRepository.update(siteId, key.id, key.value);
		
	}

	private void removeFromInstalledKeys(SiteId siteId, SSHKey key) {
		LOG.debug("Remove SSH key {} from installed keys", key);
		installedSSHKeyRepository.deleteBySSHKeyIdAndSiteId(key.id, siteId);	
	}

	private void addKeyHistory(SiteId siteId, SSHKey key) {
		LOG.debug("Add SSH key {} to history for site {} and owner {}", key.getFingerprint(), siteId,  key.ownerId.id);
		sshKeyHistoryRepository.create(SSHKeyHistory.builder().siteId(siteId)
				.originationTime(LocalDateTime.now(ZoneOffset.UTC))
				.sshkeyFingerprint(key.getFingerprint()).sshkeyOwnerId(key.ownerId).build());
		sshKeyHistoryRepository.deleteOldestLeaveOnly(siteId, key.ownerId.id, MAX_HISTORY_SIZE);
	}

	

	private void removeSSHKeyIfRemovedFromLastSite(SSHKeyOperationJob operationJob) {
		List<SSHKeyOperationJob> keysOperations = sshKeyOperationRepository.findBySSHKey(operationJob.sshkeyId);
		if (keysOperations.stream().noneMatch(o -> !o.operation.equals(REMOVE) || !o.status.equals(DONE))) {
			for (SSHKeyOperationJob job : keysOperations) {
				sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(job.sshkeyId, job.siteId);
			}
			Optional<SSHKey> removed = sshKeysRepository.findById(operationJob.sshkeyId);
			installedSSHKeyRepository.deleteBySSHKey(operationJob.sshkeyId);
			sshKeysRepository.delete(operationJob.sshkeyId);
			
			LOG.info("Removed SSH key from repository with ID={}, {}", operationJob.sshkeyId, removed.orElse(null));
		}
	}

	

}
