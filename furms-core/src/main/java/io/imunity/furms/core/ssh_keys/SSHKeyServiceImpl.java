/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;


import com.google.common.collect.Sets;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyCreatedEvent;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyRemovedEvent;
import io.imunity.furms.domain.ssh_keys.SSHKeyUpdatedEvent;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.SiteSSHKeys;
import io.imunity.furms.site.api.ssh_keys.SSHKeyAddition;
import io.imunity.furms.site.api.ssh_keys.SSHKeyUpdating;
import io.imunity.furms.site.api.ssh_keys.SiteAgentSSHKeyOperationService;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.ADD;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.REMOVE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperation.UPDATE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.DONE;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.FAILED;
import static io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus.SEND;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;


@SuppressWarnings("unused")
@Service
class SSHKeyServiceImpl implements SSHKeyService {

	private static final Logger LOG = LoggerFactory.getLogger(SSHKeyServiceImpl.class);

	private final SSHKeyRepository sshKeysRepository;
	private final SiteRepository siteRepository;
	private final SSHKeyOperationRepository sshKeyOperationRepository;
	private final SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService;
	private final SSHKeyServiceValidator validator;
	private final AuthzService authzService;
	private final UsersDAO userDao;
	private final SSHKeyFromSiteRemover sshKeyRemover;
	private final InstalledSSHKeyRepository installedSSHKeyRepository;
	private final ApplicationEventPublisher publisher;
	private final PostCommitRunner postCommitRunner;

	SSHKeyServiceImpl(SSHKeyRepository sshKeysRepository,
	                  SSHKeyServiceValidator validator,
	                  AuthzService authzService,
	                  SiteRepository siteRepository,
	                  SSHKeyOperationRepository sshKeyOperationRepository,
	                  SiteAgentSSHKeyOperationService siteAgentSSHKeyInstallationService,
	                  UsersDAO userDao,
	                  SSHKeyFromSiteRemover sshKeyRemover,
	                  InstalledSSHKeyRepository installedSSHKeyRepository,
	                  ApplicationEventPublisher publisher,
	                  PostCommitRunner postCommitRunner) {
		this.userDao = userDao;
		this.validator = validator;
		this.authzService = authzService;
		this.sshKeysRepository = sshKeysRepository;
		this.siteRepository = siteRepository;
		this.sshKeyOperationRepository = sshKeyOperationRepository;
		this.siteAgentSSHKeyInstallationService = siteAgentSSHKeyInstallationService;
		this.sshKeyRemover = sshKeyRemover;
		this.installedSSHKeyRepository = installedSSHKeyRepository;
		this.publisher = publisher;
		this.postCommitRunner = postCommitRunner;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public Optional<SSHKey> findById(SSHKeyId id) {
		LOG.debug("Getting SSH key with id={}", id);
		Optional<SSHKey> key = sshKeysRepository.findById(id);
		key.ifPresent(sshKey -> validator.validateOwner(sshKey.ownerId));
		return key;
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public Set<SSHKey> findOwned() {
		PersistentId ownerId = authzService.getCurrentUserId();
		LOG.debug("Getting all SSH keys for owner {}", ownerId);
		return sshKeysRepository.findAllByOwnerId(ownerId);
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public Set<SSHKey> findByOwnerId(PersistentId ownerId) {
		LOG.debug("Getting all SSH keys for owner {}", ownerId);
		return sshKeysRepository.findAllByOwnerId(ownerId);
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public SiteSSHKeys findSiteSSHKeysByUserIdAndSite(PersistentId userId, SiteId siteId) {
		final Set<String> sshKeys = sshKeysRepository.findAllByOwnerId(userId).stream()
				.map(key -> installedSSHKeyRepository.findBySSHKeyId(key.id))
				.flatMap(Collection::parallelStream)
				.filter(key -> key.siteId.equals(siteId))
				.map(key -> key.value)
				.collect(toSet());
		return new SiteSSHKeys(siteId, sshKeys);
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public void create(SSHKey sshKey) {
		validator.validateCreate(sshKey);
		SSHKeyId id = sshKeysRepository.create(sshKey);
		SSHKey created = sshKeysRepository.findById(id).get();
		SSHKey createdKey = sshKeysRepository.findById(id).orElseThrow(
				() -> new IllegalStateException("SSH key has not been saved to DB correctly."));
		LOG.info("Created SSHKey in repository: {}", createdKey);
		addKeyToSites(createdKey);
		publisher.publishEvent(new SSHKeyCreatedEvent(created));
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public void update(SSHKey sshKey) {
		validator.validateUpdate(sshKey);
		SSHKey oldSshKey = sshKeysRepository.findById(sshKey.id).get();
		final SSHKey oldKey = sshKeysRepository.findById(sshKey.id)
				.orElseThrow(() -> new IllegalStateException("SSH Key not found: " + sshKey.id));
		SSHKey merged = merge(oldKey, sshKey);
		updateKeyOnSites(getSiteDiff(oldKey, merged), oldKey, merged);
		SSHKeyId updatedId = sshKeysRepository.update(merged);
		LOG.info("Update SSH key in repository with ID={}, {}", sshKey.id, merged);
		publisher.publishEvent(new SSHKeyUpdatedEvent(oldSshKey, sshKey));
	}

	@Transactional
	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public void delete(SSHKeyId id) {
		validator.validateDelete(id);
		SSHKey sshKey = sshKeysRepository.findById(id).get();
		removeKeyFromSites(sshKeysRepository.findById(id).get());
		publisher.publishEvent(new SSHKeyRemovedEvent(sshKey));
	}

	private SSHKey merge(SSHKey oldKey, SSHKey key) {
		assertTrue(oldKey.id.equals(key.id),
				() -> new IllegalArgumentException("There are different SSH key during merge"));
		return SSHKey.builder().id(oldKey.id).name(key.name).value(ofNullable(key.value).orElse(oldKey.value))
			.ownerId(ofNullable(key.ownerId).orElse(oldKey.ownerId))
			.createTime(oldKey.createTime)
			.updateTime(key.updateTime)
			.sites(ofNullable(key.sites).orElse(oldKey.sites))
			.build();
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public boolean isNamePresent(String name) {
		try {
			validator.validateName(name);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@Override
	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	public boolean isNamePresentIgnoringRecord(String name, SSHKeyId recordToIgnore) {
		try {
			validator.validateIsNamePresentIgnoringRecord(name, recordToIgnore);
			return false;
		} catch (IllegalArgumentException e) {
			return true;
		}
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT)
	@Override
	public void assertIsEligibleToManageKeys() {
		validator.assertIsEligibleToManageKeys();
	}

	private void updateKeyOnSite(SSHKey oldKey, SSHKey newKey, Site site, FenixUserId userId) {
		
		if (!oldKey.getFingerprint().equals(newKey.getFingerprint()))
		{
			validator.assertKeyWasNotUsedPreviously(site, newKey);
		}
		LOG.info("Updating SSH key {} on site {}", newKey, site.getName());
		CorrelationId correlationId = CorrelationId.randomID();
		deleteOperationBySSHKeyIdAndSiteId(newKey.id, site.getId());
		createOperation(SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(site.getId()).sshkeyId(newKey.id).operation(UPDATE).status(SEND)
				.originationTime(LocalDateTime.now()).build());

		postCommitRunner.runAfterCommit(() ->
			siteAgentSSHKeyInstallationService.updateSSHKey(correlationId,
				SSHKeyUpdating.builder().siteExternalId(site.getExternalId())
					.oldPublicKey(oldKey.value).newPublicKey(newKey.value)
					.user(userId).build())
		);
	}

	private void updateKeyOnSites(SiteDiff siteDiff, SSHKey oldKey, SSHKey merged) {
		Optional<FURMSUser> user = userDao.findById(oldKey.ownerId);
		FenixUserId fenixUserId = user.get().fenixUserId.get();
		sshKeyRemover.removeKeyFromSites(oldKey, siteDiff.toRemove, fenixUserId);
		addKeyToSites(merged, siteDiff.toAdd, fenixUserId);
		updateKeyOnSites(oldKey, merged, siteDiff.toUpdate, fenixUserId);
	}

	private void updateKeyOnSites(SSHKey oldKey, SSHKey newKey, Set<SiteId> sitesIds, FenixUserId userId) {
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(toSet());

		for (Site site : sites) {

			SSHKeyOperationJob operation = sshKeyOperationRepository.findBySSHKeyIdAndSiteId(newKey.id,
					site.getId());
			if (operation == null || (operation.operation.equals(ADD) && operation.status.equals(FAILED))) {
				addKeyToSite(newKey, site, userId);
			} else {
				updateKeyOnSite(oldKey, newKey, site, userId);
			}
		}
	}

	private SiteDiff getSiteDiff(SSHKey actualKey, SSHKey newKey) {
		Set<SiteId> toAdd = Sets.newHashSet(newKey.sites);
		toAdd.removeAll(actualKey.sites != null ? (actualKey.sites.stream().filter(s -> {
			SSHKeyOperationJob job = sshKeyOperationRepository.findBySSHKeyIdAndSiteId(newKey.id, s);
			return (job.operation.equals(ADD) && job.status.equals(DONE))
					|| (job.operation.equals(UPDATE))
					|| (job.operation.equals(REMOVE));
		}).collect(toSet())) : null);

		Set<SiteId> toRemove = Sets.newHashSet(actualKey.sites);
		toRemove.removeAll(newKey.sites);
		toRemove.addAll(sshKeyOperationRepository.findBySSHKey(newKey.id).stream()
				.filter(o -> o.operation.equals(REMOVE) && o.status.equals(FAILED)).map(o -> o.siteId)
				.collect(toSet()));

		Set<SiteId> toUpdate = Sets.newHashSet();
		if (!actualKey.value.equals(newKey.value)) {
			toUpdate.addAll(actualKey.sites);
			toUpdate.retainAll(newKey.sites);
			toUpdate.removeAll(toAdd);
		} else if (actualKey.sites != null) {
			toUpdate.addAll(actualKey.sites.stream().filter(s -> {
				SSHKeyOperationJob job = sshKeyOperationRepository.findBySSHKeyIdAndSiteId(newKey.id,
						s);
				return job.operation.equals(UPDATE) && job.status.equals(FAILED);
			}).collect(toSet()));
		}

		return new SiteDiff(toAdd, toRemove, toUpdate);
	}

	private void addKeyToSites(SSHKey createdKey) {
		Optional<FURMSUser> user = userDao.findById(createdKey.ownerId);
		addKeyToSites(createdKey, createdKey.sites, user.get().fenixUserId.get());
	}

	private void removeKeyFromSites(SSHKey removedKey) {
		Optional<FURMSUser> user = userDao.findById(removedKey.ownerId);
		sshKeyRemover.removeKeyFromSites(removedKey, removedKey.sites, user.get().fenixUserId.get());
	}

	private void addKeyToSites(SSHKey sshKey, Set<SiteId> sitesIds, FenixUserId userId) {
		if(sitesIds.isEmpty())
			return;
		validator.assertUserIsInstalledOnSites(sitesIds, userId);
		Set<Site> sites = sitesIds.stream().map(s -> siteRepository.findById(s).get())
				.collect(toSet());
		for (Site site : sites) {
			addKeyToSite(sshKey, site, userId);
		}
	}

	private void addKeyToSite(SSHKey sshKey, Site site, FenixUserId userId) {

		validator.assertKeyWasNotUsedPreviously(site, sshKey);
		LOG.info("Adding SSH key {} to site {}", sshKey, site.getName());
		CorrelationId correlationId = CorrelationId.randomID();
		deleteOperationBySSHKeyIdAndSiteId(sshKey.id, site.getId());
		createOperation(SSHKeyOperationJob.builder().correlationId(correlationId)
				.siteId(site.getId()).sshkeyId(sshKey.id).operation(ADD).status(SEND)
				.originationTime(LocalDateTime.now()).build());
		postCommitRunner.runAfterCommit(() ->
			siteAgentSSHKeyInstallationService.addSSHKey(correlationId, SSHKeyAddition.builder()
				.siteExternalId(site.getExternalId()).publicKey(sshKey.value).user(userId).build())

		);
	}
	
	private void createOperation(SSHKeyOperationJob operationJob) {
		sshKeyOperationRepository.create(operationJob);
		LOG.info("SSHKeyOperationJob was created: {}", operationJob);
	}
	
	private void deleteOperationBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId) {
		sshKeyOperationRepository.deleteBySSHKeyIdAndSiteId(sshkeyId, siteId);
		LOG.info("SSHKeyOperationJob for key={} and site={} was deleted", sshkeyId, siteId);
	}

	static class SiteDiff {
		public final Set<SiteId> toAdd;
		public final Set<SiteId> toRemove;
		public final Set<SiteId> toUpdate;

		SiteDiff(Set<SiteId> toInstall, Set<SiteId> toUninstall, Set<SiteId> toReinstall) {
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