/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperation.REMOVE;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperation.UPDATE;
import static io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus.SEND;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.util.Optional.ofNullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_key_installation.SSHKeyOperationService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyAddition;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyRemoval;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyUpdating;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.site.api.site_agent.SiteAgentSSHKeyOperationService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Service
class SSHKeyServiceImpl implements SSHKeyService {

	private static final Logger LOG = LoggerFactory.getLogger(SSHKeyServiceImpl.class);

	private final SSHKeyRepository sshKeysRepository;
	private final SiteRepository siteRepository;
	private final SSHKeyOperationService sshKeyInstallationService;
	private final SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService;
	private final SSHKeyServiceValidator validator;
	private final AuthzService authzService;
	private final UsersDAO userDao;

	SSHKeyServiceImpl(SSHKeyRepository sshKeysRepository, SSHKeyServiceValidator validator,
			AuthzService authzService, SiteRepository siteRepository,
			SSHKeyOperationService sshKeyInstallationService,
			SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService, UsersDAO userDao) {
		this.userDao = userDao;
		this.validator = validator;
		this.authzService = authzService;
		this.sshKeysRepository = sshKeysRepository;
		this.siteRepository = siteRepository;
		this.sshKeyInstallationService = sshKeyInstallationService;
		this.siteAgentSSHKeyInstallationService = siteAgentSSHKeyInstallationService;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public Optional<SSHKey> findById(String id) {
		LOG.debug("Getting SSH key with id={}", id);
		Optional<SSHKey> key = sshKeysRepository.findById(id);
		if (!key.isEmpty()) {
			validator.validateOwner(key.get().ownerId);
		}
		return key;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public Set<SSHKey> findOwned() {
		PersistentId ownerId = authzService.getCurrentUserId();
		LOG.debug("Getting all SSH keys for owner {}", ownerId);
		return sshKeysRepository.findAllByOwnerId(ownerId);
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public String create(SSHKey sshKey) {
		validator.validateCreate(sshKey);
		String created = sshKeysRepository.create(sshKey);
		SSHKey createdKey = sshKeysRepository.findById(created).orElseThrow(
				() -> new IllegalStateException("SSH key has not been saved to DB correctly."));
		LOG.info("Created SSHKey in repository: {}", createdKey);

		addKeyToSites(createdKey);
		return created;
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public String update(SSHKey sshKey) {
		validator.validateUpdate(sshKey);
		final SSHKey oldKey = sshKeysRepository.findById(sshKey.id)
				.orElseThrow(() -> new IllegalStateException("SSH Key not found: " + sshKey.id));
		SSHKey merged = merge(oldKey, sshKey);
		updateKeyOnSites(getSiteDiff(oldKey, merged), oldKey, merged);
		String updatedId = sshKeysRepository.update(merged);
		LOG.info("Update SSH key in repository with ID={}, {}", sshKey.id, merged);
		return updatedId;
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public void delete(String id) {
		validator.validateDelete(id);
		removeKeyFromSites(sshKeysRepository.findById(id).get());
	}

	private SSHKey merge(SSHKey oldKey, SSHKey key) {
		assertTrue(oldKey.id.equals(key.id),
				() -> new IllegalArgumentException("There are different SSH key during merge"));
		return SSHKey.builder().id(oldKey.id).name(key.name).value(ofNullable(key.value).orElse(oldKey.value))
				.ownerId(ofNullable(key.ownerId).orElse(oldKey.ownerId)).createTime(oldKey.createTime)
				.updateTime(key.updateTime).sites(ofNullable(key.sites).orElse(oldKey.sites)).build();
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public boolean isNamePresent(String name) {
		try {
			validator.validateName(name);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	public boolean isNamePresentIgnoringRecord(String name, String recordToIgnore) {
		try {
			validator.validateIsNamePresentIgnoringRecord(name, recordToIgnore);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	private void updateKeyOnSites(SSHKey oldKey, SSHKey newKey, Set<String> sitesIds, FenixUserId userId) {
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(Collectors.toSet());

		for (Site site : sites) {
			updateKeyOnSite(oldKey, newKey, site, userId);
		}
	}

	private void updateKeyOnSite(SSHKey oldKey, SSHKey newKey, Site site, FenixUserId userId) {
		LOG.debug("Updating SSH key {} on site {}", newKey.name, site.getName());
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		sshKeyInstallationService.deleteBySSHKeyIdAndSiteId(newKey.id, site.getId());

		sshKeyInstallationService.create(SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(site.getId()).sshkeyId(newKey.id).operation(UPDATE).status(SEND).build());
		siteAgentSSHKeyInstallationService.updateSSHKey(correlationId,
				SSHKeyUpdating.builder().siteExternalId(site.getExternalId()).oldPublicKey(oldKey.value)
						.newPublicKey(newKey.value).user(userId).build());

	}

	private void updateKeyOnSites(SiteDiff siteDiff, SSHKey oldKey, SSHKey merged) {
		Optional<FURMSUser> user = userDao.findById(oldKey.ownerId);
		FenixUserId fenixUserId = user.get().fenixUserId.get();
		removeKeyFromSites(oldKey, siteDiff.toRemove, fenixUserId);
		addKeyToSites(merged, siteDiff.toAdd, fenixUserId);
		updateKeyOnSites(oldKey, merged, siteDiff.toUpdate, fenixUserId);
	}

	private SiteDiff getSiteDiff(SSHKey actualKey, SSHKey newKey) {
		Set<String> toAdd = Sets.newHashSet(newKey.sites);
		toAdd.removeAll(actualKey.sites);

		Set<String> toRemove = Sets.newHashSet(actualKey.sites);
		toRemove.removeAll(newKey.sites);

		Set<String> toUpdate = Sets.newHashSet();
		if (actualKey.value != newKey.value) {
			toUpdate.addAll(actualKey.sites);
			toUpdate.retainAll(newKey.sites);
		}

		return new SiteDiff(toAdd, toRemove, toUpdate);
	}

	private void addKeyToSites(SSHKey createdKey) {
		Optional<FURMSUser> user = userDao.findById(createdKey.ownerId);
		addKeyToSites(createdKey, createdKey.sites, user.get().fenixUserId.get());
	}

	private void removeKeyFromSites(SSHKey createdKey) {
		Optional<FURMSUser> user = userDao.findById(createdKey.ownerId);
		removeKeyFromSites(createdKey, createdKey.sites, user.get().fenixUserId.get());
	}

	private void addKeyToSites(SSHKey sshKey, Set<String> sitesIds, FenixUserId userId) {
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(Collectors.toSet());

		for (Site site : sites) {
			addKeyToSite(sshKey, site, userId);
		}
	}

	private void removeKeyFromSites(SSHKey sshKey, Set<String> sitesIds, FenixUserId userId) {
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(Collectors.toSet());

		for (Site site : sites) {
			removeKeyFromSite(sshKey, site, userId);
		}
	}

	private void addKeyToSite(SSHKey sshKey, Site site, FenixUserId userId) {

		LOG.debug("Adding SSH key {} on site {}", sshKey.name, site.getName());
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());

		sshKeyInstallationService.deleteBySSHKeyIdAndSiteId(sshKey.id, site.getId());
		
		sshKeyInstallationService.create(SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(site.getId()).sshkeyId(sshKey.id).operation(ADD).status(SEND).build());

		siteAgentSSHKeyInstallationService.addSSHKey(correlationId, SSHKeyAddition.builder()
				.siteExternalId(site.getExternalId()).publicKey(sshKey.value).user(userId).build());

	}

	private void removeKeyFromSite(SSHKey sshKey, Site site, FenixUserId userId) {

		LOG.debug("Removing SSH key {} from site {}", sshKey.name, site.getName());
		CorrelationId correlationId = new CorrelationId(UUID.randomUUID().toString());
		sshKeyInstallationService.deleteBySSHKeyIdAndSiteId(sshKey.id, site.getId());

		sshKeyInstallationService.create(SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(site.getId()).sshkeyId(sshKey.id).operation(REMOVE).status(SEND).build());
		siteAgentSSHKeyInstallationService.removeSSHKey(correlationId, SSHKeyRemoval.builder()
				.siteExternalId(site.getExternalId()).publicKey(sshKey.value).user(userId).build());
	}

	private static class SiteDiff {
		public final Set<String> toAdd;
		public final Set<String> toRemove;
		public final Set<String> toUpdate;

		SiteDiff(Set<String> toInstall, Set<String> toUninstall, Set<String> toReinstall) {
			this.toAdd = Set.copyOf(toInstall);
			this.toRemove = Set.copyOf(toUninstall);
			this.toUpdate = Set.copyOf(toReinstall);
		}

		@Override
		public int hashCode() {
			return Objects.hash(toAdd, toRemove, toUpdate);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SiteDiff other = (SiteDiff) obj;
			return Objects.equals(toAdd, other.toAdd) && Objects.equals(toRemove, other.toRemove)
					&& Objects.equals(toUpdate, other.toUpdate);
		}		
	}
}