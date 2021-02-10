/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.unity.users;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;
import io.imunity.furms.domain.users.UserAttribute;
import io.imunity.furms.domain.users.UserAttributes;
import io.imunity.furms.unity.client.UnityClient;
import pl.edu.icm.unity.types.basic.Attribute;

public class UnityUsersDAOTest {
	@SuppressWarnings("unchecked")
	@Test
	public void shouldParseRootAttributes() {
		UnityClient unityClient = mock(UnityClient.class);
		when(unityClient.getWithListParam(eq("/entity/user1/groups/attributes"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Map.of("/", List.of(new Attribute("attr1", "string", "/", List.of("val1")))));
		when(unityClient.get(eq("/entity/user1/groups"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Set.of("/"));
		UnityUsersDAO unityUsersDAO = new UnityUsersDAO(unityClient);
		
		UserAttributes userAttributes = unityUsersDAO.getUserAttributes("user1");
		
		assertThat(userAttributes.rootAttributes).containsExactlyInAnyOrder(
				new UserAttribute("attr1", "val1"));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldParseCommunityResourceAttributes() {
		UUID id = UUID.randomUUID();
		String idStr = id.toString();
		UnityClient unityClient = mock(UnityClient.class);
		when(unityClient.getWithListParam(eq("/entity/user1/groups/attributes"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Map.of("/fenix/communities/" + id + "/users", 
					List.of(new Attribute("attr1", "string", "/fenix/communities/" + id + "/users", List.of("val1")))));
		when(unityClient.get(eq("/entity/user1/groups"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Set.of("/", "/fenix", "/fenix/communities", 
					"/fenix/communities/" + id, "/fenix/communities/" + id + "/users"));
		UnityUsersDAO unityUsersDAO = new UnityUsersDAO(unityClient);
		
		UserAttributes userAttributes = unityUsersDAO.getUserAttributes("user1");
		
		assertThat(userAttributes.attributesByResource).containsEntry(new ResourceId(idStr, ResourceType.COMMUNITY),
				Set.of(new UserAttribute("attr1", "val1")));	
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldParseProjectResourceAttributes() {
		UUID id = UUID.randomUUID();
		UUID idC = UUID.randomUUID();
		String idStr = id.toString();
		UnityClient unityClient = mock(UnityClient.class);
		when(unityClient.getWithListParam(eq("/entity/user1/groups/attributes"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Map.of("/fenix/communities/foo/projects/" + id + "/users", 
					List.of(new Attribute("attr1", "string", "/fenix/communities/" + idC + "/projects/" + id + "/users", List.of("val1")))));
		when(unityClient.get(eq("/entity/user1/groups"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Set.of("/", "/fenix", "/fenix/communities", 
					"/fenix/communities/" + idC, 
					"/fenix/communities/" + idC + "/projects",
					"/fenix/communities/" + idC + "/projects/" + id, 
					"/fenix/communities/" + idC + "/projects/" + id + "/users"));
		UnityUsersDAO unityUsersDAO = new UnityUsersDAO(unityClient);
		
		UserAttributes userAttributes = unityUsersDAO.getUserAttributes("user1");
		
		assertThat(userAttributes.attributesByResource).containsEntry(new ResourceId(idStr, ResourceType.PROJECT),
				Set.of(new UserAttribute("attr1", "val1")));	
	}

	
	@SuppressWarnings("unchecked")
	@Test
	public void shouldAppendGroupsWithoutAttributes() {
		UUID id = UUID.randomUUID();
		String idStr = id.toString();
		UnityClient unityClient = mock(UnityClient.class);
		when(unityClient.getWithListParam(eq("/entity/user1/groups/attributes"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Collections.emptyMap());
		when(unityClient.get(eq("/entity/user1/groups"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Set.of("/", "/fenix", "/fenix/communities", 
					"/fenix/communities/" + id));
		UnityUsersDAO unityUsersDAO = new UnityUsersDAO(unityClient);
		
		UserAttributes userAttributes = unityUsersDAO.getUserAttributes("user1");
		
		assertThat(userAttributes.attributesByResource).containsEntry(new ResourceId(idStr, ResourceType.COMMUNITY),
				Collections.emptySet());	
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldConvertEmailAttribute() {
		UnityClient unityClient = mock(UnityClient.class);
		when(unityClient.getWithListParam(eq("/entity/user1/groups/attributes"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Map.of("/", List.of(new Attribute("attr1", "verifiableEmail", "/", 
					List.of("{\"value\":\"test@example.com\",\"confirmationData\":{\"confirmed\":false,\"confirmationDate\":0,\"sentRequestAmount\":0},\"tags\":[]}")))));
		when(unityClient.get(eq("/entity/user1/groups"), 
				any(ParameterizedTypeReference.class), any()))
			.thenReturn(Set.of("/"));
		UnityUsersDAO unityUsersDAO = new UnityUsersDAO(unityClient);
		
		UserAttributes userAttributes = unityUsersDAO.getUserAttributes("user1");
		
		assertThat(userAttributes.rootAttributes).containsExactlyInAnyOrder(
				new UserAttribute("attr1", "test@example.com"));
	}

}
