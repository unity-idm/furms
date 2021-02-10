/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;

class UnityGroupParserTest {

	private final UUID id = UUID.randomUUID();

	@Test
	public void shouldReturnUuidAndResourceTypeSite(){
		ResourceId resourceId = UnityGroupParser.getResourceId("/fenix/sites/" + id + "/users");
		assertEquals(id, resourceId.id);
		assertEquals(ResourceType.SITE, resourceId.type);
	}

	@Test
	public void shouldReturnUuidAndResourceTypeCommunity(){
		ResourceId resourceId = UnityGroupParser.getResourceId("/fenix/communities/" + id + "/users");
		assertEquals(id, resourceId.id);
		assertEquals(ResourceType.COMMUNITY, resourceId.type);
	}

	@Test
	public void shouldReturnUuidAndResourceTypeProject(){
		UUID communityId = UUID.randomUUID();
		ResourceId resourceId = UnityGroupParser.getResourceId("/fenix/communities/" + communityId + "/projects/"
			+ id +"/users");
		assertEquals(id, resourceId.id);
		assertEquals(ResourceType.PROJECT, resourceId.type);
	}

	@Test
	public void shouldReturnUuidAndResourceTypeAll(){
		ResourceId resourceId = UnityGroupParser.getResourceId("/fenix/users");
		assertNull(resourceId.id);
		assertEquals(ResourceType.APP_LEVEL, resourceId.type);
	}

	@Test
	public void shouldThrowsExceptionWhenGroupIsNotCorrect(){
		assertThrows(
			IllegalArgumentException.class,
			() -> UnityGroupParser.getResourceId("/fenix/")
		);
	}

	@Test
	public void shouldThrowsExceptionWhenGroupIsNull(){
		assertThrows(
			IllegalArgumentException.class,
			() -> UnityGroupParser.getResourceId(null)
		);
	}
}