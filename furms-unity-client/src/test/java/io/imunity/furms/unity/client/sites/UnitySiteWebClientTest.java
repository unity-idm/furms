/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.unity.client.unity.UnityClient;
import io.imunity.furms.unity.client.unity.UnityEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class UnitySiteWebClientTest {

	@Mock
	private UnityClient unityClient;

	@Mock
	private SiteEndpoints siteEndpoints;

	@Mock
	private UnityEndpoints unityEndpoints;

	@InjectMocks
	private UnitySiteWebClient unitySiteWebClient;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);

		when(siteEndpoints.getBaseDecoded()).thenReturn("baseDecoded");
		when(siteEndpoints.getBaseEncoded()).thenReturn("baseEncoded");
		when(siteEndpoints.getMeta()).thenReturn("meta");
		when(siteEndpoints.getUsers()).thenReturn("users");
		when(unityEndpoints.getGroup()).thenReturn("group");
	}

	@Test
	void shouldGetMetaInfoAboutSite() {
		//given
		String id = UUID.randomUUID().toString();
		Group group = new Group("/path/"+id);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(eq(siteEndpoints.getMeta()), eq(Group.class), anyMap())).thenReturn(group);

		//when
		Optional<Site> site = unitySiteWebClient.get(id);

		//then
		assertThat(site).isPresent();
		assertThat(site.get().getId()).isEqualTo(id);
		assertThat(site.get().getName()).isEqualTo("test");
	}

}