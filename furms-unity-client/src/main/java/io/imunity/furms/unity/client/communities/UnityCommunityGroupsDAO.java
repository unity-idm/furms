/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.communities;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.unity.client.communities.exceptions.UnityCommunityOperationException;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.META;
import static io.imunity.furms.unity.client.communities.UnityCommunityPaths.FENIX_COMMUNITY_ID;
import static io.imunity.furms.unity.client.communities.UnityCommunityPaths.FENIX_COMMUNITY_ID_USERS;
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
				.pathSegment(FENIX_COMMUNITY_ID)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			Group group = unityClient.get(path, Group.class);
			return Optional.ofNullable(CommunityGroup.builder()
					.id(id)
					.name(group.getDisplayedName().getDefaultValue())
					.build());
		} catch (WebClientResponseException e) {
			if (HttpStatus.valueOf(e.getRawStatusCode()).is5xxServerError()) {
				throw e;
			}
			return Optional.empty();
		}
	}

	@Override
	public void create(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not create Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String groupPath = UriComponentsBuilder.newInstance()
				.path(FENIX_COMMUNITY_ID)
				.uriVariables(uriVariables)
				.toUriString();
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(community.getName()));
		try {
			unityClient.post(GROUP_BASE, group);
		} catch (WebClientException e) {
			throw new UnityCommunityOperationException(e.getMessage());
		}
		try {
			String createCommunityUsersPath = UriComponentsBuilder.newInstance()
					.path(GROUP_BASE)
					.pathSegment(groupPath + FENIX_COMMUNITY_ID_USERS)
					.toUriString();
			unityClient.post(createCommunityUsersPath);
		} catch (WebClientException e) {
			this.delete(community.getId());
			throw new UnityCommunityOperationException(e.getMessage());
		}
	}

	@Override
	public void update(CommunityGroup community) {
		if (community == null || isEmpty(community.getId())) {
			throw new IllegalArgumentException("Could not update Community in Unity. Missing Community or Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(community);
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_COMMUNITY_ID)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			Group group = unityClient.get(metaCommunityPath, Group.class);
			group.setDisplayedName(new I18nString(community.getName()));
			unityClient.put(GROUP_BASE, group);
		} catch (WebClientException e) {
			throw new UnityCommunityOperationException(e.getMessage());
		}
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Missing Community ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, Object> queryParams = Map.of("recursive", TRUE);
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_COMMUNITY_ID)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			unityClient.delete(deleteCommunityPath, queryParams);
		} catch (WebClientException e) {
			throw new UnityCommunityOperationException(e.getMessage());
		}
	}

	private Map<String, Object> uriVariables(CommunityGroup community) {
		return uriVariables(community.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of("id", id);
	}

}
