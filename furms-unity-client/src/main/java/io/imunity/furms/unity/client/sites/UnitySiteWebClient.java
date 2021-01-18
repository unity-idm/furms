/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.common.UnityPaths.GROUP_BASE;
import static io.imunity.furms.unity.client.common.UnityPaths.META;
import static io.imunity.furms.unity.client.sites.UnitySitePaths.FENIX_SITE_ID;
import static io.imunity.furms.unity.client.sites.UnitySitePaths.FENIX_SITE_ID_USERS;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

@Component
class UnitySiteWebClient implements SiteWebClient {

	private final UnityClient unityClient;

	UnitySiteWebClient(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public Optional<Site> get(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Could not get Site from Unity. Missing Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		String path = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_SITE_ID)
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
			throw new UnityFailureException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public void create(Site site) {
		if (site == null || isEmpty(site.getId())) {
			throw new IllegalArgumentException("Could not create Site in Unity. Missing Site or Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(site);
		String groupPath = UriComponentsBuilder.newInstance()
				.path(FENIX_SITE_ID)
				.uriVariables(uriVariables)
				.toUriString();
		Group group = new Group(groupPath);
		group.setDisplayedName(new I18nString(site.getName()));
		try {
			unityClient.post(GROUP_BASE, group);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e.getCause());
		}
		try {
			String createSiteUsersPath = UriComponentsBuilder.newInstance()
					.path(GROUP_BASE)
					.pathSegment(groupPath + FENIX_SITE_ID_USERS)
					.toUriString();
			unityClient.post(createSiteUsersPath);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public void update(Site site) {
		if (site == null || isEmpty(site.getId())) {
			throw new IllegalArgumentException("Could not update Site in Unity. Missing Site or Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(site);
		String metaSitePath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_SITE_ID)
				.path(META)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			Group group = unityClient.get(metaSitePath, Group.class);
			group.setDisplayedName(new I18nString(site.getName()));
			unityClient.put(GROUP_BASE, group);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e.getCause());
		}
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Missing Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, Object> queryParams = Map.of("recursive", TRUE);
		String deleteSitePath = UriComponentsBuilder.newInstance()
				.path(GROUP_BASE)
				.pathSegment(FENIX_SITE_ID)
				.uriVariables(uriVariables)
				.buildAndExpand().encode().toUriString();
		try {
			unityClient.delete(deleteSitePath, queryParams);
		} catch (WebClientResponseException e) {
			throw new UnityFailureException(e.getMessage(), e.getCause());
		}
	}

	private Map<String, Object> uriVariables(Site site) {
		return uriVariables(site.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of("id", id);
	}
}
