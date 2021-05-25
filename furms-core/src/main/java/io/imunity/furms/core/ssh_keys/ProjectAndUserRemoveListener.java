/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.domain.projects.RemoveProjectEvent;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.RemoveUserRoleEvent;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;

@Component
class ProjectAndUserRemoveListener {

	private static final Logger LOG = LoggerFactory.getLogger(ProjectAndUserRemoveListener.class);

	private UsersDAO usersDAO;
	private final UserOperationRepository userOperationRepository;
	private final SSHKeyRepository sshKeyRepository;
	private final SSHKeyFromSiteRemover sshKeyRemover;

	ProjectAndUserRemoveListener(UsersDAO usersDAO, UserOperationRepository userOperationRepository,
			SSHKeyRepository sshKeyRepository, SSHKeyFromSiteRemover sshKeyRemover) {

		this.usersDAO = usersDAO;
		this.userOperationRepository = userOperationRepository;
		this.sshKeyRepository = sshKeyRepository;
		this.sshKeyRemover = sshKeyRemover;
	}

	@EventListener
	void onProjectRemove(RemoveProjectEvent removeProjectEvent) {
		LOG.debug("RemoveProjectEvent received: {}", removeProjectEvent);
		for (FURMSUser user : removeProjectEvent.projectUsers) {
			processUser(user.id.get(), user.fenixUserId.get(), removeProjectEvent.id);
		}
	}

	@EventListener
	void onUserRoleRemove(RemoveUserRoleEvent removeUserRoleEvent) {
		LOG.debug("RemoveUserRoleEvent received: {}", removeUserRoleEvent);
		FenixUserId fenixUserId = usersDAO.getFenixUserId(removeUserRoleEvent.id);
		if (fenixUserId == null) {
			LOG.error("Can not get user sites, user {} without fenix id", removeUserRoleEvent.id);
			return;
		}

		processUser(removeUserRoleEvent.id, fenixUserId, removeUserRoleEvent.resourceId.id.toString());
	}

	@Transactional
	private void processUser(PersistentId userId, FenixUserId fenixId, String projectId) {
		Set<String> userSites = findUserSites(fenixId, projectId);	
		Set<SSHKey> userKeys = sshKeyRepository.findAllByOwnerId(userId);

		for (SSHKey sshKey : userKeys) {
			removeFromSitesAndUpdateKey(sshKey, userSites, fenixId, projectId);
		}
	}

	private void removeFromSitesAndUpdateKey(SSHKey sshKey, Set<String> userSites, FenixUserId fenixId,
			String projectId) {
		Set<String> keySitesToRemove = new HashSet<>(sshKey.sites);
		keySitesToRemove.removeAll(userSites);
		if (keySitesToRemove.isEmpty()) {
			return;
		}

		Set<String> keySitesToUpdate = new HashSet<>(sshKey.sites);
		keySitesToUpdate.removeAll(keySitesToRemove);
		SSHKey toUpdate = SSHKey.builder().id(sshKey.id).name(sshKey.name).value(sshKey.value)
				.ownerId(sshKey.ownerId).createTime(sshKey.createTime).updateTime(sshKey.updateTime)
				.sites(keySitesToUpdate).build();
		sshKeyRepository.update(toUpdate);
		sshKeyRemover.removeKeyFromSites(sshKey, keySitesToRemove, fenixId);

	}

	private Set<String> findUserSites(FenixUserId fenixUserId, String skippedProjectId) {
		return userOperationRepository.findAllUserAdditions(fenixUserId.id).stream()
				.filter(ua -> !ua.projectId.equals(skippedProjectId)).map(ua -> ua.siteId.id)
				.collect(Collectors.toSet());
	}
}
