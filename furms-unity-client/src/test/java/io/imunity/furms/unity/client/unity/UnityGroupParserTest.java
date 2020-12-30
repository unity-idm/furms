/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import io.imunity.furms.domain.roles.ResourceId;
import io.imunity.furms.domain.roles.ResourceType;
import io.imunity.furms.unity.client.unity.exception.WrongGroupException;
import org.junit.jupiter.api.Test;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UnityGroupParserTest {

	private final UUID id = UUID.randomUUID();

	@Test
	void name() {
		AntPathMatcher matcher = new AntPathMatcher();
		List<String> collect = List.of("/fenix/users", "/", "/fenix/sites/alalalala/users").stream()
			.filter(x -> matcher.match("/fenix/**/users", x))
			.collect(Collectors.toList());
		System.out.println(collect);
	}

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
			WrongGroupException.class,
			() -> UnityGroupParser.getResourceId("/fenix/")
		);
	}

	@Test
	public void shouldThrowsExceptionWhenGroupIsNull(){
		assertThrows(
			WrongGroupException.class,
			() -> UnityGroupParser.getResourceId(null)
		);
	}
}