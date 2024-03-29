/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.sites;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.AllUsersAndSiteAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.sites.SiteGroupDAO;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import io.imunity.rest.api.types.basic.RestGroup;
import io.imunity.rest.api.types.basic.RestI18nString;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.unity.common.UnityConst.FENIX_GROUP;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.SITE_PATTERN;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;
import static io.imunity.furms.utils.ValidationUtils.assertTrue;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.ObjectUtils.isEmpty;

@Component
class UnitySiteGroupDAO implements SiteGroupDAO {

	private static final String RECURSIVE_PARAM = "recursive";

	private final UnityClient unityClient;
	private final UserService userService;

	public UnitySiteGroupDAO(UnityClient unityClient, UserService userService) {
		this.unityClient = unityClient;
		this.userService = userService;
	}

	@Override
	public Optional<Site> get(SiteId id) {
		assertTrue(!isEmpty(id), () -> new IllegalArgumentException("Could not get Site from Unity. Missing Site ID"));
		Map<String, Object> uriVariables = uriVariables(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(SITE_PATTERN)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			RestGroup group = unityClient.get(path, RestGroup.class);
			return Optional.ofNullable(Site.builder()
					.id(id)
					.name(group.displayedName.defaultValue)
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
		RestGroup group = RestGroup.builder()
			.withPath(groupPath)
			.withDisplayedName(RestI18nString.builder().withDefaultValue(site.getName()).build())
			.build();
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
			RestGroup group = unityClient.get(metaSitePath, RestGroup.class);
			RestGroup restGroup = RestGroup.builder()
				.withPath(group.path)
				.withDisplayedName(RestI18nString.builder().withDefaultValue(site.getName()).build())
				.withI18nDescription(group.i18nDescription)
				.withDescription(group.description)
				.withAttributeStatements(group.attributeStatements)
				.withAttributesClasses(group.attributesClasses)
				.withDelegationConfiguration(group.delegationConfiguration)
				.withPublicGroup(group.publicGroup)
				.withProperties(group.properties)
				.build();
			unityClient.put(GROUP_BASE, restGroup);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public void delete(SiteId id) {
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
	public List<FURMSUser> getSiteUsers(SiteId siteId, Set<Role> roles) {
		assertTrue(!isEmpty(siteId),
				() -> new IllegalArgumentException("Could not get Site Administrators from Unity. Missing Site ID"));
		String sitePath = getSitePath(siteId);
		return userService.getAllUsersByRoles(sitePath, roles);
	}

	@Override
	public AllUsersAndSiteAdmins getAllUsersAndSiteAdmins(SiteId siteId) {
		assertTrue(!isEmpty(siteId),
			() -> new IllegalArgumentException("Could not get Site Administrators from Unity. Missing Site ID"));
		String sitePath = getSitePath(siteId);
		GroupedUsers groupedUsers = userService.getUsersFromGroupsFilteredByRoles(
			Map.of(
				FENIX_GROUP,
				Set.of(),
				sitePath,
				Set.of(Role.SITE_ADMIN)
			));
		return new AllUsersAndSiteAdmins(groupedUsers.getUsers(FENIX_GROUP), groupedUsers.getUsers(sitePath));
	}

	@Override
	public void addSiteUser(SiteId siteId, PersistentId userId, Role role) {
		assertTrue(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site role in Unity. Missing Site ID or User ID"));

		String group = getSitePath(siteId);
		userService.addUserToGroup(userId, group);
		userService.addUserRole(userId, group, role);
	}

	@Override
	public void removeSiteUser(SiteId siteId, PersistentId userId) {
		removeSiteRole(siteId, userId);
	}

	private void removeSiteRole(SiteId siteId, PersistentId userId) {
		assertTrue(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove Site role in Unity. Missing Site ID or User ID"));

		String group = getSitePath(siteId);

		userService.removeUserFromGroup(userId, group);
	}

	private Map<String, Object> uriVariables(Site site) {
		return uriVariables(site.getId());
	}

	private Map<String, Object> uriVariables(SiteId id) {
		return Map.of(ID, id.id.toString());
	}

	private String getSitePath(SiteId siteId) {
		return UriComponentsBuilder.newInstance()
				.path(SITE_PATTERN)
				.path(USERS_PATTERN)
				.buildAndExpand(Map.of(ID, siteId.id.toString()))
				.encode().toUriString();
	}
}
