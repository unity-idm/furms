/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.generic_groups;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.spi.audit_log.AuditLogRepository;
import io.imunity.furms.spi.generic_groups.GenericGroupRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@SpringBootApplication(scanBasePackages = "io.imunity.furms.core.audit_log", scanBasePackageClasses = GenericGroupAuditLogService.class)
class GenericGroupAuditLogServiceIntegrationTest {
	@MockBean
	private GenericGroupRepository genericGroupRepository;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private Clock fixedClock;

	@MockBean
	private AuthzService authzService;
	@MockBean
	private ObjectMapper objectMapper;
	@Autowired
	private ApplicationEventPublisher publisher;
	@MockBean
	private AuditLogRepository auditLogRepository;

	private GenericGroupServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new GenericGroupServiceImpl(genericGroupRepository, usersDAO, fixedClock, publisher);
	}

	@Test
	void shouldDetectGroupDeletion() {
		GenericGroupId groupId = new GenericGroupId(UUID.randomUUID());
		when(genericGroupRepository.existsBy("communityId", groupId)).thenReturn(true);
		GenericGroup genericGroup = GenericGroup.builder().build();
		when(genericGroupRepository.findBy(groupId)).thenReturn(Optional.of(genericGroup));

		service.delete("communityId", groupId);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectGroupUpdate() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.id(genericGroupId)
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();
		when(genericGroupRepository.findBy(genericGroupId)).thenReturn(Optional.of(genericGroup));

		service.update(genericGroup);

		Mockito.verify(auditLogRepository).create(any());
	}

	@Test
	void shouldDetectGroupCreation() {
		GenericGroupId genericGroupId = new GenericGroupId(UUID.randomUUID());
		GenericGroup genericGroup = GenericGroup.builder()
			.communityId("communityId")
			.name("name")
			.description("description")
			.build();

		when(genericGroupRepository.create(genericGroup)).thenReturn(genericGroupId);
		when(genericGroupRepository.findBy(genericGroupId)).thenReturn(Optional.of(genericGroup));

		GenericGroupId createdGenericGroupId = service.create(genericGroup);

		Mockito.verify(auditLogRepository).create(any());
	}
}
