/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.resource.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
			RuntimeException.class,
			() -> UnityGroupParser.getResourceId("/fenix/"),
			"Group should contain at least two elements"
		);
	}

	@Test
	public void shouldThrowsExceptionWhenGroupIsNull(){
		assertThrows(
			RuntimeException.class,
			() -> UnityGroupParser.getResourceId(null),
			"Group cannot be a null"
		);
	}
}