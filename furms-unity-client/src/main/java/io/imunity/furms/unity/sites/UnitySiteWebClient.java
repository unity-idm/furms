/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.sites;

import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static io.imunity.furms.unity.common.UnityConst.GROUP_PATH;
import static io.imunity.furms.unity.common.UnityConst.ID;
import static io.imunity.furms.unity.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.common.UnityConst.PERSISTENT_IDENTITY;
import static io.imunity.furms.unity.common.UnityConst.SITE_PATTERN;
import static io.imunity.furms.unity.common.UnityPaths.ATTRIBUTE_PATTERN;
import static io.imunity.furms.unity.common.UnityPaths.ENTITY_BASE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.common.UnityPaths.GROUP_MEMBERS;
import static io.imunity.furms.unity.common.UnityPaths.META;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;
import static io.imunity.furms.utils.ValidationUtils.check;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.users.UnityUserMapper;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.Group;
import pl.edu.icm.unity.types.basic.GroupMember;

@Component
class UnitySiteWebClient implements SiteWebClient {

	private static final String RECURSIVE_PARAM = "recursive";
	private static final String ENUM_ATTRIBUTE_VALUE_SYNTAX = "enumeration";

	private final UnityClient unityClient;

	UnitySiteWebClient(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Optional<Site> get(String id) {
		check(!isEmpty(id), () -> new IllegalArgumentException("Could not get Site from Unity. Missing Site ID"));
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
		check(site != null && !isEmpty(site.getId()),
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
		check(site != null && !isEmpty(site.getId()), () -> new IllegalArgumentException("Could not update Site in Unity. Missing Site or Site ID"));
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
		check(!isEmpty(id), () -> new IllegalArgumentException("Could not delete Site from Unity. Missing Site ID"));
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
	public List<User> getAllAdmins(String id) {
		check(!isEmpty(id),
				() -> new IllegalArgumentException("Could not get Site Administrators from Unity. Missing Site ID"));
		String sitePath = siteUsersPath(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_MEMBERS)
				.pathSegment("{" + GROUP_PATH + "}")
				.buildAndExpand(Map.of(GROUP_PATH, sitePath))
				.encode()
				.toUriString();

		return unityClient.get(path, new ParameterizedTypeReference<List<GroupMember>>() {}).stream()
				.filter(groupMember -> groupMember.getAttributes().stream()
						.anyMatch(attribute -> attribute.getName().equals(SITE_ADMIN.unityRoleAttribute)
											&& attribute.getValues().contains(SITE_ADMIN.unityRoleValue)))
				.map(UnityUserMapper::map)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	@Override
	public void addAdmin(String siteId, String userId) {
		check(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not add Site Administrator in Unity. Missing Site ID or User ID"));
		Map<String, String> identityTypeParam = Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY);

		String addAdminPath = siteAdminPath(siteId, userId);
		try {
			unityClient.post(addAdminPath, identityTypeParam);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}

		Attribute attribute = new Attribute(SITE_ADMIN.unityRoleAttribute,
				ENUM_ATTRIBUTE_VALUE_SYNTAX,
				siteUsersPath(siteId),
				List.of(SITE_ADMIN.unityRoleValue));
		String setAttributePath = UriComponentsBuilder.newInstance()
				.path(ENTITY_BASE)
				.path("{" + ID + "}")
				.path(ATTRIBUTE_PATTERN)
				.buildAndExpand(Map.of(ID, userId))
				.encode().toUriString();
		try {
			unityClient.put(setAttributePath, attribute, identityTypeParam);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e);
		}
	}

	@Override
	public void removeAdmin(String siteId, String userId) {
		check(!isEmpty(siteId) && !isEmpty(userId),
				() -> new IllegalArgumentException("Could not remove Site Administrator in Unity. Missing Site ID or User ID"));

		String path = siteAdminPath(siteId, userId);
		unityClient.delete(path, Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY));
	}

	private Map<String, Object> uriVariables(Site site) {
		return uriVariables(site.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of(ID, id);
	}

	private String siteAdminPath(String siteId, String userId) {
		String sitePath = siteUsersPath(siteId);
		return UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment("{" + GROUP_PATH + "}")
				.path(ENTITY_BASE)
				.path("{" + ID + "}")
				.buildAndExpand(Map.of(GROUP_PATH, sitePath, ID, userId))
				.encode()
				.toUriString();
	}

	private String siteUsersPath(String siteId) {
		return UriComponentsBuilder.newInstance()
				.path(SITE_PATTERN)
				.path(USERS_PATTERN)
				.buildAndExpand(Map.of(ID, siteId))
				.encode().toUriString();
	}
}
