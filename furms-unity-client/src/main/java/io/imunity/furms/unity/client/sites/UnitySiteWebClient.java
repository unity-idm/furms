/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteWebClient;
import io.imunity.furms.unity.client.sites.exceptions.UnitySiteCreateException;
import io.imunity.furms.unity.client.sites.exceptions.UnitySiteDeleteException;
import io.imunity.furms.unity.client.sites.exceptions.UnitySiteUpdateException;
import io.imunity.furms.unity.client.unity.UnityClient;
import io.imunity.furms.unity.client.unity.UnityEndpoints;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Map;
import java.util.Optional;

import static io.imunity.furms.unity.client.unity.UriVariableUtils.buildPath;
import static java.lang.Boolean.TRUE;
import static org.springframework.util.StringUtils.isEmpty;

@Component
class UnitySiteWebClient implements SiteWebClient {

	private final UnityClient unityClient;
	private final SiteEndpoints siteEndpoints;
	private final UnityEndpoints unityEndpoints;

	UnitySiteWebClient(UnityClient unityClient, SiteEndpoints siteEndpoints, UnityEndpoints unityEndpoints) {
		this.unityClient = unityClient;
		this.siteEndpoints = siteEndpoints;
		this.unityEndpoints = unityEndpoints;
	}

	@Override
	public Optional<Site> get(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Could not get Site from Unity. Missing Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		try {
			Group group = unityClient.get(siteEndpoints.getMeta(), Group.class, uriVariables);
			return Optional.ofNullable(Site.builder()
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
	public void create(Site site) {
		if (site == null || isEmpty(site.getId())) {
			throw new IllegalArgumentException("Could not create Site in Unity. Missing Site or Site ID");
		}
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
	public void update(Site site) {
		if (site == null || isEmpty(site.getId())) {
			throw new IllegalArgumentException("Could not update Site in Unity. Missing Site or Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(site);
		try {
			Group group = unityClient.get(siteEndpoints.getMeta(), Group.class, uriVariables);
			group.setDisplayedName(new I18nString(site.getName()));
			unityClient.put(unityEndpoints.getGroup(), group, uriVariables);
		} catch (WebClientException e) {
			throw new UnitySiteUpdateException(e.getMessage());
		}
	}

	@Override
	public void delete(String id) {
		if (isEmpty(id)) {
			throw new IllegalArgumentException("Missing Site ID");
		}
		Map<String, Object> uriVariables = uriVariables(id);
		Map<String, Object> queryParams = Map.of("recursive", TRUE);
		try {
			unityClient.delete(siteEndpoints.getBaseEncoded(), uriVariables, queryParams);
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
