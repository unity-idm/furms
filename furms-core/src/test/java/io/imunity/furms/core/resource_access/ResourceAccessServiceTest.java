/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;

class ResourceAccessServiceTest {
	@Mock
	private SiteAgentResourceAccessService siteAgentResourceAccessService;
	@Mock
	private ResourceAccessRepository repository;
	@Mock
	private UserOperationRepository userRepository;

	private ResourceAccessService service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ResourceAccessServiceImpl(siteAgentResourceAccessService, repository, userRepository);
		orderVerifier = inOrder(repository, siteAgentResourceAccessService);
	}

	@Test
	void shouldGrantAccess() {
		GrantAccess grantAccess = GrantAccess.builder()
			.build();
		//when
		service.grantAccess(grantAccess);

		//then
		orderVerifier.verify(repository).create(any(), eq(grantAccess));
		orderVerifier.verify(siteAgentResourceAccessService).grantAccess(any(), eq(grantAccess));
	}

	@Test
	void shouldRevokeAccess() {
		GrantAccess grantAccess = GrantAccess.builder()
			.build();
		//when
		service.revokeAccess(grantAccess);

		//then
		orderVerifier.verify(repository).update(any(), eq(grantAccess));
		orderVerifier.verify(siteAgentResourceAccessService).revokeAccess(any(), eq(grantAccess));
	}
}