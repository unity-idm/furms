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
import java.util.Optional;
import java.util.stream.Collectors;

import static io.imunity.furms.unity.client.common.UnityConst.*;
import static io.imunity.furms.unity.client.common.UnityPaths.*;

@Component
class UnityUsersDAO implements UsersDAO {

	private final UnityClient unityClient;

	UnityUsersDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public List<User> getAdminUsers() {
		return getUsers(FENIX_USERS_GROUP);
	}

	@Override
	public List<User> getAllUsers() {
		return getUsers(FENIX_GROUP);
	}

	private List<User> getUsers(String usersGroupPath) {
		Map<String, String> uriVariables = Map.of(ROOT_GROUP_PATH, usersGroupPath);
		String path = UriComponentsBuilder.newInstance()
			.path(GROUP_MEMBERS)
			.pathSegment("{" + ROOT_GROUP_PATH + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}).stream()
			.map(UnityUserMapper::map)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	@Override
	public void addFenixAdminRole(String userId) {
		String path = prepareRequestPath(userId);
		unityClient.post(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	@Override
	public void removeFenixAdminRole(String userId) {
		String path = prepareRequestPath(userId);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	private String prepareRequestPath(String userId) {
		Map<String, String> uriVariables = Map.of(GROUP_PATH, FENIX_USERS_GROUP, ID, userId);
		return UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment("{" + GROUP_PATH + "}")
			.path(ENTITY_BASE)
			.pathSegment("{" + ID + "}")
			.buildAndExpand(uriVariables)
			.encode()
			.toUriString();
	}
}
