/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.inOrder;

class ResourceAccessMessageResolverTest {
	@Mock
	private ResourceAccessRepository repository;

	private ResourceAccessMessageResolverImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ResourceAccessMessageResolverImpl(repository);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldUpdateUserAddition() {
		CorrelationId correlationId = CorrelationId.randomID();

		service.update(correlationId, AccessStatus.GRANTED, null);

		orderVerifier.verify(repository).update(correlationId, AccessStatus.GRANTED, null);
	}
}