/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.sites;

import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.SITE_PATTERN;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

@Component
class UnitySiteWebClient implements SiteWebClient {

	private static final String RECURSIVE_PARAM = "recursive";

	private final UnityClient unityClient;
	private final UserService userService;

	public UnitySiteWebClient(UnityClient unityClient, UserService userService) {
		this.unityClient = unityClient;
		this.userService = userService;
	}

	@Override
	public Optional<Site> get(String id) {
		assertTrue(!isEmpty(id), () -> new IllegalArgumentException("Could not get Site from Unity. Missing Site ID"));
		Map<String, Object> uriVariables = uriVariables(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(SITE_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			Group group = unityClient.get(path, Group.class);
			return Optional.ofNullable(Site.builder()
					.id(id)
					.name(group.getDisplayedName().getDefaultValue())
					.build());
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public void create(Site site) {
		assertTrue(site != null && !isEmpty(site.getId()),
				() -> new IllegalArgumentException("Could not create Site in Unity. Missing Site or Site ID"));
		Map<String, Object> uriVariables = uriVariables(site);
		String groupPath = UriComponentsBuilder.newInstance()
				.path(SITE_PATTERN)
				.uriVariables(uriVariables)
				.toUriString();
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(site.getName()));
		try {
			unityClient.post(GROUP_BASE, group);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
		try {
			String createSiteUsersPath = UriComponentsBuilder.newInstance()
					.path(GROUP_BASE)
					.pathSegment(groupPath + USERS_PATTERN)
					.toUriString();
			unityClient.post(createSiteUsersPath);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public void update(Site site) {
		assertTrue(site != null && !isEmpty(site.getId()), 
				() -> new IllegalArgumentException("Could not update Site in Unity. Missing Site or Site ID."));
		Map<String, Object> uriVariables = uriVariables(site);
		String metaSitePath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(SITE_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			Group group = unityClient.get(metaSitePath, Group.class);
			group.setDisplayedName(new I18nString(site.getName()));
			unityClient.put(GROUP_BASE, group);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(String id) {
		assertTrue(!isEmpty(id), () -> new IllegalArgumentException("Could not delete Site from Unity. Missing Site ID"));
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, String> queryParams = Map.of(RECURSIVE_PARAM, TRUE.toString());
		String deleteSitePath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(SITE_PATTERN)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			unityClient.delete(deleteSitePath, queryParams);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}


	@Override
	public List<FURMSUser> getAllAdmins(String siteId) {
		assertTrue(!isEmpty(siteId),
				() -> new IllegalArgumentException("Could not get Site Administrators from Unity. Missing Site ID"));
		String sitePath = getSitePath(siteId);
		return userService.getAllUsersByRole(sitePath, SITE_ADMIN);
	}

	@Override
	public void addAdmin(String siteId, PersistentId userId) {
		assertTrue(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator in Unity. Missing Site ID or User ID"));

		String group = getSitePath(siteId);
		userService.addUserToGroup(userId, group);
		userService.addUserRole(userId, group, SITE_ADMIN);
	}

	@Override
	public void removeAdmin(String siteId, PersistentId userId) {
		assertTrue(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove Site Administrator in Unity. Missing Site ID or User ID"));

		String group = getSitePath(siteId);
		Set<String> roleValues = userService.getRoleValues(userId, group, SITE_ADMIN);
		if (roleValues.contains(SITE_ADMIN.unityRoleValue)) {
			if (roleValues.size() == 1)
				userService.removeUserFromGroup(userId, group);
			else
				userService.removeUserRole(userId, group, SITE_ADMIN);
		}
	}

	private Map<String, Object> uriVariables(Site site) {
		return uriVariables(site.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of(ID, id);
	}

	private String getSitePath(String siteId) {
		return UriComponentsBuilder.newInstance()
				.path(SITE_PATTERN)
				.path(USERS_PATTERN)
				.buildAndExpand(Map.of(ID, siteId))
				.encode().toUriString();
	}
}
