/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import static io.imunity.furms.domain.authz.roles.Capability.OWNED_SSH_KEY_MANAGMENT;
import static io.imunity.furms.domain.authz.roles.ResourceType.APP_LEVEL;

import java.lang.invoke.MethodHandles;
import java.util.List;

import org.apache.sshd.common.util.SshdEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.imunity.furms.api.ssh_keys.SSHKeyHistoryService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.constant.SSHKeysConst;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;

@Service
class SSHKeyHistoryServiceImpl implements SSHKeyHistoryService, SshdEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SSHKeyHistoryRepository repository;

	SSHKeyHistoryServiceImpl(SSHKeyHistoryRepository repository) {
		this.repository = repository;
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Transactional
	@Override
	public void create(SSHKeyHistory sshKeyHistory) {
		repository.create(sshKeyHistory);
		LOG.info("SSHKeyHistory was created: {}", sshKeyHistory);
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Transactional
	@Override
	public List<SSHKeyHistory> findLastBySSHKeyIdLimitTo(String siteId, int limit) {
		return repository.findLastBySSHKeyIdLimitTo(siteId, limit);
	}

	@FurmsAuthorize(capability = OWNED_SSH_KEY_MANAGMENT, resourceType = APP_LEVEL)
	@Transactional
	@Override
	public void deleteOldestLeaveOnly(String siteId, int leave) {
		repository.deleteOldestLeaveOnly(siteId, leave);
		LOG.info("SSHKeyHistory for site {} was reduced to {} ", siteId, SSHKeysConst.MAX_HISTORY_SIZE);
	}

}
