/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

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
		CorrelationId correlationId = CorrelationId.randomID();
		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(UserStatus.ADDING_ACKNOWLEDGED);

		service.update(UserAddition.builder()
			.correlationId(correlationId)
			.build());

		orderVerifier.verify(repository).update(any(UserAddition.class));
	}

	@Test
	void shouldUpdateUserAdditionStatus() {
		CorrelationId correlationId = CorrelationId.randomID();

		when(repository.findAdditionStatusByCorrelationId(correlationId.id)).thenReturn(UserStatus.ADDING_ACKNOWLEDGED);
		service.updateStatus(correlationId, UserStatus.ADDING_PENDING, "msg");

		orderVerifier.verify(repository).updateStatus(correlationId, UserStatus.ADDING_PENDING, "msg");
	}
}