/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_usage;

import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

class ResourceUsageUpdaterTest {
	@Mock
	private ResourceUsageRepository repository;

	private ResourceUsageUpdaterImpl service;
	private InOrder orderVerifier;

	@BeforeEach
	void init() {
		MockitoAnnotations.initMocks(this);
		service = new ResourceUsageUpdaterImpl(repository);
		orderVerifier = inOrder(repository);
	}

	@Test
	void shouldUpdateResourceUsage() {
		ResourceUsage resourceUsage = ResourceUsage.builder()
			.projectId("id")
			.projectAllocationId("id")
			.cumulativeConsumption(BigDecimal.TEN)
			.probedAt(LocalDateTime.now().minusMinutes(5))
			.build();

		service.updateUsage(resourceUsage);

		verify(repository).create(resourceUsage);
	}

	@Test
	void shouldUpdateUserResourceUsage() {
		UserResourceUsage resourceUsage = UserResourceUsage.builder()
			.projectId("id")
			.projectAllocationId("id")
			.fenixUserId(new FenixUserId("userId"))
			.cumulativeConsumption(BigDecimal.TEN)
			.consumedUntil(LocalDateTime.now().minusMinutes(5))
			.build();

		service.updateUsage(resourceUsage);

		verify(repository).create(resourceUsage);
	}

}