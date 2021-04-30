/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;

class UserOperationMessageResolverTest {
	@Mock
	private UserOperationRepository repository;

	private UserOperationMessageResolverImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new UserOperationMessageResolverImpl(repository);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldUpdateUserAddition() {
		service.update(UserAddition.builder().build());
		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@Test
	void shouldUpdateUserAdditionStatus() {
		CorrelationId correlationId = CorrelationId.randomID();
		service.updateStatus(correlationId, UserAdditionStatus.PENDING);
		orderVerifier.verify(repository).updateStatus(correlationId, UserAdditionStatus.PENDING);
	}

	@Test
	void shouldUpdateUserRemovalStatus() {
		CorrelationId correlationId = CorrelationId.randomID();
		service.updateStatus(correlationId, UserRemovalStatus.PENDING);
		orderVerifier.verify(repository).updateStatus(correlationId, UserRemovalStatus.PENDING);
	}
}