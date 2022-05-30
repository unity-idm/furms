/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.projects.ProjectRemovedEvent;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserProjectMembershipRevokedEvent;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProjectAndUserRemoveListener {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectAndUserRemoveListener.class);

	private final UsersDAO usersDAO;
	private final UserOperationRepository userOperationRepository;
	private final SSHKeyRepository sshKeyRepository;
	private final SSHKeyHistoryRepository sshKeyHistoryRepository;
	private final SSHKeyFromSiteRemover sshKeyRemover;

	ProjectAndUserRemoveListener(UsersDAO usersDAO, UserOperationRepository userOperationRepository,
			SSHKeyRepository sshKeyRepository, SSHKeyFromSiteRemover sshKeyRemover,
			SSHKeyHistoryRepository sshKeyHistoryRepository) {

		this.usersDAO = usersDAO;
		this.userOperationRepository = userOperationRepository;
		this.sshKeyRepository = sshKeyRepository;
		this.sshKeyRemover = sshKeyRemover;
		this.sshKeyHistoryRepository = sshKeyHistoryRepository;
	}

	@Async
	@EventListener
	@Transactional
	public void onProjectRemove(ProjectRemovedEvent projectRemovedEvent) {
		LOG.debug("RemoveProjectEvent received: {}", projectRemovedEvent);
		for (FURMSUser user : projectRemovedEvent.projectUsers) {
			try {
				processUser(user.id.get(), user.fenixUserId.get(), projectRemovedEvent.project.getId());
			} catch (Exception e) {
				LOG.error("Can not remove sites from ssh keys owned by user {} after project {} remove",
						user, projectRemovedEvent.project.getId());
			}
		}
	}

	@Async
	@EventListener
	@Transactional
	public void onUserRoleRemove(UserProjectMembershipRevokedEvent removeUserRoleEvent) {
		LOG.debug("RemoveUserProjectMembershipEvent received: {}", removeUserRoleEvent);
		FenixUserId fenixUserId = usersDAO.getFenixUserId(removeUserRoleEvent.id);
		if (fenixUserId == null) {
			LOG.error("Can not get user sites, user {} without fenix id", removeUserRoleEvent.id);
			return;
		}
		try {
			processUser(removeUserRoleEvent.id, fenixUserId, new ProjectId(removeUserRoleEvent.resourceId.id));
		} catch (Exception e) {
			LOG.error("Can not remove sites from ssh keys owned by user {} after project {} remove",
					removeUserRoleEvent.id, removeUserRoleEvent.resourceId.id.toString());
		}
	}

	private void processUser(PersistentId userId, FenixUserId fenixId, ProjectId projectId) {
		Set<SiteId> userSites = findUserSites(fenixId, projectId);
		Set<SSHKey> userKeys = sshKeyRepository.findAllByOwnerId(userId);

		for (SSHKey sshKey : userKeys) {
			removeFromSitesAndUpdateKey(sshKey, userSites, fenixId, userId, projectId);
		}
	}

	private void removeFromSitesAndUpdateKey(SSHKey sshKey, Set<SiteId> userSites, FenixUserId fenixId,
	                                         PersistentId persistentId,
			ProjectId projectId) {
		Set<SiteId> keySitesToRemove = new HashSet<>(sshKey.sites);
		keySitesToRemove.removeAll(userSites);
		if (keySitesToRemove.isEmpty()) {
			return;
		}

		Set<SiteId> keySitesToUpdate = new HashSet<>(sshKey.sites);
		keySitesToUpdate.removeAll(keySitesToRemove);
		SSHKey toUpdate = SSHKey.builder().id(sshKey.id).name(sshKey.name).value(sshKey.value)
				.ownerId(sshKey.ownerId).createTime(sshKey.createTime).updateTime(sshKey.updateTime)
				.sites(keySitesToUpdate).build();
		sshKeyRepository.update(toUpdate);
		keySitesToRemove.forEach(s -> sshKeyHistoryRepository.deleteLatest(s, persistentId.id));
		sshKeyRemover.removeKeyFromSites(sshKey, keySitesToRemove, fenixId);

	}

	private Set<SiteId> findUserSites(FenixUserId fenixUserId, ProjectId skippedProjectId) {
		return userOperationRepository.findAllUserAdditions(fenixUserId).stream()
			.filter(ua -> !ua.projectId.equals(skippedProjectId))
			.map(ua -> ua.siteId)
			.collect(Collectors.toSet());
	}
}
