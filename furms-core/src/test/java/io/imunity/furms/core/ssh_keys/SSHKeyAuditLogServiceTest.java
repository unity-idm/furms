/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.ssh_keys;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyCreatedEvent;
import io.imunity.furms.domain.ssh_keys.SSHKeyUpdatedEvent;
import io.imunity.furms.domain.users.PersistentId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Set;


@ExtendWith(MockitoExtension.class)
class SSHKeyAuditLogServiceTest {

	@Mock
	private AuthzService authzService;
	@Mock
	private ApplicationEventPublisher publisher;

	private SSHKeyAuditLogService service;

	@BeforeEach
	void setUp() {
		service = new SSHKeyAuditLogService(authzService, publisher, new ObjectMapper());
	}

	@Test
	void onSSHKeyCreatedEvent() {
		SSHKey oldKey = getKey("name", Set.of("s1"));
		service.onSSHKeyCreatedEvent(new SSHKeyCreatedEvent(oldKey));
	}

	@Test
	void onSSHKeyRemovedEvent() {
	}

	@Test
	void onSSHKeyUpdatedEvent() {
		SSHKey oldKey = getKey("name", Set.of("s1"));
		SSHKey newKey = getKey("lolo", Set.of("s2"));
		service.onSSHKeyUpdatedEvent(new SSHKeyUpdatedEvent(oldKey, newKey));
	}

	private SSHKey getKey(String name, Set<String> sites) {
		return SSHKey.builder().id("id").name(name).value(
				"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDvFdnmjLkBdvUqojB/fWMGol4PyhUHgRCn6/Hiaz/pnedck"
					+ "Spgh+RvDor7UsU8bkOQBYc0Yr1ETL1wUR1vIFxqTm23JmmJsyO5EJgUw92nVIc0gj1u5q6xRKg3ONnxEXhJD/78OSp/Z"
					+ "Y8dJw4fnEYl22LfvGXIuCZbvtKNv1Az19y9LU57kDBi3B2ZBDn6rjI6sTeO2jDzb0m0HR1jbLzBO43sxqnVHC7yf9DM7Tp"
					+ "bbgd1Q2km5eySfit/5E3EJBYY4PvankHzGts1NCblK8rX6w+MlV5L1pVZkstVF6hn9gMSM0fInvpJobhQ5KzcL8sJTKO5AL"
					+ "mb9xUkdFjZk9bL demo@demo.pl")
			.ownerId(new PersistentId("id")).sites(sites).build();
	}
}