/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.users;

import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.basic.GroupMember;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.imunity.furms.unity.client.common.UnityPaths.*;

@Component
class UnityUsersDAO implements UsersDAO {

	private final UnityClient unityClient;

	UnityUsersDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public List<User> getAllUsers() {
		Map<String, String> uriVariables = Map.of("rootGroupPath", "/fenix");
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS)
			.pathSegment("{rootGroupPath}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}).stream()
			.map(UnityUserMapper::map)
			.collect(Collectors.toList());
	}

	@Override
	public void addUserToAdminGroup(String userId) {
		Map<String, String> uriVariables = Map.of("groupPath", "/fenix/users", "id", userId);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{groupPath}")
			.path(ENTITY_BASE)
			.pathSegment("{id}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		unityClient.post(path);
	}

	@Override
	public void deleteUser(String userId) {
		Map<String, String> uriVariables = Map.of("id", userId);
		String path = UriComponentsBuilder.newInstance()
			.path(ENTITY_BASE)
			.pathSegment("{id}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		unityClient.delete(path, Map.of());
	}
}
