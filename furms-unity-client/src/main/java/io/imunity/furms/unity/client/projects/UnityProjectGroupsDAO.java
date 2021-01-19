/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.projects;

import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityPaths.*;
import static io.imunity.furms.unity.client.projects.UnityProjectPaths.FENIX_PROJECT_PATTERN;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

@Component
class UnityProjectGroupsDAO implements ProjectGroupsDAO {

	private final UnityClient unityClient;

	UnityProjectGroupsDAO(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Optional<ProjectGroup> get(String communityId, String projectId) {
		if (isEmpty(communityId) || isEmpty(projectId)) {
			throw new IllegalArgumentException("Could not get Project from Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(communityId, projectId);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_PROJECT_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();

		Group group = unityClient.get(path, Group.class);
		return Optional.ofNullable(ProjectGroup.builder()
			.id(projectId)
			.name(group.getDisplayedName().getDefaultValue())
			.build());
	}

	@Override
	public void create(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not create Project in Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String groupPath = UriComponentsBuilder.newInstance()
				.path(FENIX_PROJECT_PATTERN)
				.uriVariables(uriVariables)
				.toUriString();
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(projectGroup.getName()));
		unityClient.post(GROUP_BASE, group);
		String createCommunityUsersPath = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment(groupPath + USERS_PATTERN)
			.toUriString();
		unityClient.post(createCommunityUsersPath);
	}

	@Override
	public void update(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not update Project in Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_PROJECT_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		Group group = unityClient.get(metaCommunityPath, Group.class);
		group.setDisplayedName(new I18nString(projectGroup.getName()));
		unityClient.put(GROUP_BASE, group);
	}

	@Override
	public void delete(String communityId, String projectId) {
		if (isEmpty(communityId) || isEmpty(projectId)) {
			throw new IllegalArgumentException("Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(communityId, projectId);
		Map<String, Object> queryParams = Map.of("recursive", TRUE);
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_PROJECT_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		unityClient.delete(deleteCommunityPath, queryParams);
	}

	private Map<String, Object> getUriVariables(String communityId, String projectId) {
		return Map.of("communityId", communityId, "projectId", projectId);
	}
}
