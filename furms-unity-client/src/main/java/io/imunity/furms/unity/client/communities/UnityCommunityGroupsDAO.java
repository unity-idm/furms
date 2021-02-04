/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.communities;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityConst.*;
import static io.imunity.furms.unity.client.common.UnityPaths.*;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

@Component
class UnityCommunityGroupsDAO implements CommunityGroupsDAO {

	private final UnityClient unityClient;

	UnityCommunityGroupsDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Optional<CommunityGroup> get(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Could not get Community from Unity. Missing Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();

		Group group = unityClient.get(path, Group.class);
		return Optional.ofNullable(CommunityGroup.builder()
			.id(id)
			.name(group.getDisplayedName().getDefaultValue())
			.build());
	}

	@Override
	public void create(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not create Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String groupPath = UriComponentsBuilder.newInstance()
				.path(COMMUNITY_PATTERN)
				.uriVariables(uriVariables)
				.toUriString();
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(community.getName()));
		unityClient.post(GROUP_BASE, group);
		String createCommunityUsersPath = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment(groupPath + USERS_PATTERN)
			.toUriString();unityClient.post(createCommunityUsersPath);
	}

	@Override
	public void update(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not update Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		Group group = unityClient.get(metaCommunityPath, Group.class);
		group.setDisplayedName(new I18nString(community.getName()));
		unityClient.put(GROUP_BASE, group);
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Missing Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, Object> queryParams = Map.of(RECURSIVE, TRUE);
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(COMMUNITY_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		unityClient.delete(deleteCommunityPath, queryParams);
	}

	private Map<String, Object> uriVariables(CommunityGroup community) {
		return uriVariables(community.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of(ID, id);
	}

}
