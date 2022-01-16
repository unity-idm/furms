/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.api.key;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Component
public class RemoveApiKeyListener {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final AuthzService authzService;
	private final UserApiKeyRepository userApiKeyRepository;

	public RemoveApiKeyListener(AuthzService authzService, UserApiKeyRepository userApiKeyRepository) {
		this.authzService = authzService;
		this.userApiKeyRepository = userApiKeyRepository;
	}

	@Async
	@EventListener
	@Transactional
	public void onUserAdminRoleRemoval(UserRoleRevokedEvent userRoleRevokedEvent) {
		LOG.debug("RemoveProjectEvent received: {}", userRoleRevokedEvent);
		try {
			if (!authzService.hasRESTAPITokensCreationRights(userRoleRevokedEvent.getId())) {
				userApiKeyRepository.delete(userRoleRevokedEvent.id);
			}
		} catch (Exception e) {
			LOG.error("Can not remove API KEY for user event", e);
		}
	}

}
