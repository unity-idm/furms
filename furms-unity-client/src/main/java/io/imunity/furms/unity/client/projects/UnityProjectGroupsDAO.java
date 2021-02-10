/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.projects;

import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityConst.*;
import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.META;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

@Component
class UnityProjectGroupsDAO implements ProjectGroupsDAO {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
				.pathSegment(PROJECT_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();

		Group group = unityClient.get(path, Group.class);
		return Optional.ofNullable(ProjectGroup.builder()
			.id(projectId)
			.name(group.getDisplayedName().getDefaultValue())
			.communityId(communityId)
			.build());
	}

	@Override
	public void create(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not create Project in Unity. Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String groupPath = UriComponentsBuilder.newInstance()
			.path(GROUP_BASE)
			.pathSegment(PROJECT_PATTERN)
			.uriVariables(uriVariables)
			.buildAndExpand().encode().toUriString();

		unityClient.post(groupPath, null, Map.of(WITH_PARENTS, TRUE.toString()));
		updateGroupName(projectGroup);
		LOG.debug("Project group {} under Community group {} was crated in Unity", projectGroup.getId(), projectGroup.getCommunityId());
	}

	@Override
	public void update(ProjectGroup projectGroup) {
		if (projectGroup == null || isEmpty(projectGroup.getId()) || isEmpty(projectGroup.getCommunityId())) {
			throw new IllegalArgumentException("Could not update Project in Unity. Missing Community or Project ID");
		}
		updateGroupName(projectGroup);
		LOG.debug("Project group {} name was updated to: {}", projectGroup.getId(), projectGroup.getName());

	}

	private void updateGroupName(ProjectGroup projectGroup) {
		Map<String, Object> uriVariables = getUriVariables(projectGroup.getCommunityId(), projectGroup.getId());
		String metaCommunityPath = UriComponentsBuilder.newInstance()
				.path(PROJECT_GROUP_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().toUriString();
		Group group = new Group(metaCommunityPath);
		group.setDisplayedName(new I18nString(projectGroup.getName()));
		unityClient.put(GROUP_BASE, group);
	}

	@Override
	public void delete(String communityId, String projectId) {
		if (isEmpty(communityId) || isEmpty(projectId)) {
			throw new IllegalArgumentException("Missing Community or Project ID");
		}
		Map<String, Object> uriVariables = getUriVariables(communityId, projectId);
		Map<String, String> queryParams = Map.of(RECURSIVE, TRUE.toString());
		String deleteCommunityPath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(PROJECT_GROUP_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		unityClient.delete(deleteCommunityPath, queryParams);
		LOG.debug("Project group {} under Community group {} was deleted", projectId, communityId);
	}

	private Map<String, Object> getUriVariables(String communityId, String projectId) {
		return Map.of(COMMUNITY_ID, communityId, PROJECT_ID, projectId);
	}
}
