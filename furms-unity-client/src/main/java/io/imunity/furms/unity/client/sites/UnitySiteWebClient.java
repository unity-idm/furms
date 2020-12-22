/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.unity.client.sites.exceptions.UnitySiteCreateException;
import io.imunity.furms.unity.client.sites.exceptions.UnitySiteDeleteException;
import io.imunity.furms.unity.client.unity.UnityClient;
import io.imunity.furms.unity.client.unity.UnityEndpoints;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;

import static io.imunity.furms.unity.client.unity.UriVariableUtils.buildPath;

@Component
class UnitySiteWebClient implements SiteWebClient {

	private final static boolean RECURSIVE = true;

	private final UnityClient unityClient;
	private final SiteEndpoints siteEndpoints;
	private final UnityEndpoints unityEndpoints;

	UnitySiteWebClient(UnityClient unityClient, SiteEndpoints siteEndpoints, UnityEndpoints unityEndpoints) {
		this.unityClient = unityClient;
		this.siteEndpoints = siteEndpoints;
		this.unityEndpoints = unityEndpoints;
	}

	@Override
	public Site get(String id) {
		Map<String, Object> uriVariables = uriVariables(id);
		Group group = unityClient.get(siteEndpoints.getMeta(), Group.class, uriVariables);

		return Site.builder()
				.id(id)
				.name(group.getDisplayedName().getDefaultValue())
				.build();
	}

	@Override
	public void create(Site site) {
		Map<String, Object> idUriVariable = uriVariables(site);
		Group group = new Group(buildPath(siteEndpoints.getBaseDecoded(), idUriVariable));
		group.setDisplayedName(new I18nString(site.getName()));
		try {
			unityClient.post(unityEndpoints.getGroup(), group, idUriVariable);
		} catch (WebClientException e) {
			throw new UnitySiteCreateException(e.getMessage());
		}
		try {
			unityClient.post(siteEndpoints.getUsers(), idUriVariable);
		} catch (WebClientException e) {
			this.delete(site.getId());
			throw new UnitySiteCreateException(e.getMessage());
		}
	}

	@Override
	public void delete(String id) {
		Map<String, Object> uriVariables = uriVariables(id);
		try {
			unityClient.delete(siteEndpoints.getBaseEncoded(), RECURSIVE, uriVariables);
		} catch (WebClientException e) {
			throw new UnitySiteDeleteException(e.getMessage());
		}
	}

	private Map<String, Object> uriVariables(Site site) {
		return uriVariables(site.getId());
	}

	private Map<String, Object> uriVariables(String id) {
		return Map.of("id", id);
	}

}
