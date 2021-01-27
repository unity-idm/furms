/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.unity.client.unity.UnityClient;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

class UnitySiteWebClientTest {

	@Mock
	private UnityClient unityClient;

	@InjectMocks
	private UnitySiteWebClient unitySiteWebClient;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	void shouldGetMetaInfoAboutSite() {
		//given
		String id = UUID.randomUUID().toString();
		Group group = new Group("/path/"+id);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(id), eq(Group.class))).thenReturn(group);

		//when
		Optional<Site> site = unitySiteWebClient.get(id);

		//then
		assertThat(site).isPresent();
		assertThat(site.get().getId()).isEqualTo(id);
		assertThat(site.get().getName()).isEqualTo("test");
	}

	@Test
	void shouldCreateSite() {
		//given
		Site site = Site.builder()
				.id(UUID.randomUUID().toString())
				.name("test")
				.build();
		doNothing().when(unityClient).post(contains(site.getId()), any());
		doNothing().when(unityClient).post(contains("users"), any());

		//when
		unitySiteWebClient.create(site);

		//then
		verify(unityClient, times(1)).post(anyString(), any());
		verify(unityClient, times(1)).post(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	void shouldExpectExceptionWhenCommunicationWithUnityIsBroken() {
		//given
		WebClientResponseException webException = new WebClientResponseException(400, "BAD_REQUEST", null, null, null);
		doThrow(webException).when(unityClient).get(anyString(), any(Class.class));
		doThrow(webException).when(unityClient).post(anyString());
		doThrow(webException).when(unityClient).delete(anyString(), any());

		//when + then
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.get("id"));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.create(Site.builder().id("id").build()));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.update(Site.builder().id("id").build()));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.delete("id"));
	}

	@Test
	void shouldUpdateSite() {
		//given
		Site site = Site.builder()
				.id(UUID.randomUUID().toString())
				.name("test")
				.build();
		Group group = new Group("/path/"+site.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(site.getId()), eq(Group.class))).thenReturn(group);
		doNothing().when(unityClient).put(contains(site.getId()), eq(Group.class));

		//when
		unitySiteWebClient.update(site);

		//then
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveSite() {
		//given
		String id = UUID.randomUUID().toString();
		doNothing().when(unityClient).delete(contains(id), anyMap());

		//when
		unitySiteWebClient.delete(id);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

}