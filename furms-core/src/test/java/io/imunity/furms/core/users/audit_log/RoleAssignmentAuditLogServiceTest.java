/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.audit_log;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.AuditLogEvent;
import io.imunity.furms.domain.audit_log.Operation;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserRoleRevokedEvent;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleAssignmentAuditLogServiceTest {
	@Mock
	private AuthzService authzService;
	@Mock
	private UsersDAO usersDAO;
	@Mock
	private ApplicationEventPublisher publisher;
	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private RoleAssignmentAuditLogService roleAssignmentAuditLogService;

	@Test
	void shouldPublishEvent() {
		PersistentId id = new PersistentId("id");
		when(usersDAO.findById(id)).thenReturn(Optional.of(FURMSUser.builder()
			.id(id)
			.email("email")
			.build())
		);

		roleAssignmentAuditLogService.onGenericGroupCreatedEvent(new UserRoleRevokedEvent(
			id,
			new ResourceId(UUID.randomUUID(), ResourceType.APP_LEVEL),
			"",
			Role.FENIX_ADMIN
		));

		ArgumentCaptor<AuditLogEvent> argument = ArgumentCaptor.forClass(AuditLogEvent.class);
		Mockito.verify(publisher).publishEvent(argument.capture());
		assertEquals(Operation.ROLE_ASSIGNMENT, argument.getValue().auditLog.operationCategory);
		assertEquals(Action.REVOKE, argument.getValue().auditLog.action);
	}
}
