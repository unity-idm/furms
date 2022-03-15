/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.core.utils.InvokeAfterCommitEvent;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.ssh_keys.SSHKeyRemoval;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.REMOVE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.FAILED;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.SEND;

@Component
class SSHKeyFromSiteRemover {

	private static final Logger LOG = LoggerFactory.getLogger(SSHKeyFromSiteRemover.class);

	private final SSHKeyRepository sshKeysRepository;
	private final SiteRepository siteRepository;
	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService;
	private final ApplicationEventPublisher publisher;

	SSHKeyFromSiteRemover(SSHKeyRepository sshKeysRepository, SiteRepository siteRepository,
			SSHKeyOperationRepository sshKeyOperationRepository,
			SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService, ApplicationEventPublisher publisher) {
		this.sshKeysRepository = sshKeysRepository;
		this.siteRepository = siteRepository;
		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.siteAgentSSHKeyInstallationService = siteAgentSSHKeyInstallationService;
		this.publisher = publisher;
	}
	
	@Transactional
	public void removeKeyFromSites(SSHKey sshKey, Set<String> sitesIds, FenixUserId userId) {
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(Collectors.toSet());
		LOG.debug("Removing SSHKey {} from sites {}", sshKey, sites);
		boolean removeFromSite = false;
		for (Site site : sites) {
			SSHKeyOperationJob operation;
			try {
				operation = sshKeyOperationRepository.findBySSHKeyIdAndSiteId(sshKey.id, site.getId());
			} catch (Exception e) {
				LOG.error("Can not get ssh key operation for key {}", sshKey.id);
				return;
			}
			if (operation.operation.equals(ADD) && operation.status.equals(FAILED)) {
				deleteOperationBySSHKeyIdAndSiteId(sshKey.id, site.getId());

			} else {
				removeFromSite = true;
				removeKeyFromSite(sshKey, site, userId);
			}
		}

		if (!removeFromSite && sshKeyOperationRepository.findBySSHKey(sshKey.id).isEmpty()) {
			sshKeysRepository.delete(sshKey.id);

		}
	}

	private void removeKeyFromSite(SSHKey sshKey, Site site, FenixUserId userId) {

		LOG.info("Removing SSH key {} from site {}", sshKey, site.getName());
		CorrelationId correlationId = CorrelationId.randomID();
		deleteOperationBySSHKeyIdAndSiteId(sshKey.id, site.getId());
		createOperation(SSHKeyOperationJob.builder().correlationId(correlationId).siteId(site.getId())
				.sshkeyId(sshKey.id).operation(REMOVE).status(SEND).originationTime(LocalDateTime.now())
				.build());
		publisher.publishEvent(new InvokeAfterCommitEvent(() -> siteAgentSSHKeyInstallationService.removeSSHKey(correlationId,
			SSHKeyRemoval.builder().siteExternalId(site.getExternalId()).publicKey(sshKey.value)
				.user(userId).build())));

	}

	private void createOperation(SSHKeyOperationJob operationJob) {
		sshKeyOperationRepository.create(operationJob);
		LOG.info("SSHKeyOperationJob was created: {}", operationJob);
	}

	private void deleteOperationBySSHKeyIdAndSiteId(String sshkeyId, String siteId) {
		sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(sshkeyId, siteId);
		LOG.info("SSHKeyOperationJob for key={} and site={} was deleted", sshkeyId, siteId);
	}
}
