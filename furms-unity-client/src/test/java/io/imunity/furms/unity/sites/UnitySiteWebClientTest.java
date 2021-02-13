/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.sites;

import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static io.imunity.furms.domain.authz.roles.Role.SITE_SUPPORT;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

class UnitySiteWebClientTest {

	@Mock
	private UnityClient unityClient;

	@Mock
	private UserService userService;

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

	@Test
	void shouldGetSiteAdministrators() {
		//given
		String siteId = UUID.randomUUID().toString();
		String groupPath = "/fenix/sites/"+ siteId +"/users";
		when(userService.getAllUsersByRole(groupPath, SITE_ADMIN))
			.thenReturn(List.of(
				new User("1", "firstName", "lastName", "email"),
				new User("3", "firstName", "lastName", "email"))
			);

		//when
		List<User> admins = unitySiteWebClient.getAllAdmins(siteId);

		//then
		assertThat(admins).hasSize(2);
		assertThat(admins.stream()
				.allMatch(user -> user.id.equals("1") || user.id.equals("3"))).isTrue();
	}

	@Test
	void shouldAddAdminToSite() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		String groupPath = "/fenix/sites/"+ siteId +"/users";
		//when
		unitySiteWebClient.addAdmin(siteId, userId);

		//then
		verify(userService, times(1)).addUserRole(eq(userId), eq(groupPath), eq(SITE_ADMIN));
		verify(userService, times(1)).addUserToGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminFromGroup() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		String groupPath = "/fenix/sites/"+ siteId +"/users";

		//when
		when(userService.getRoleValues(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
			.thenReturn(Set.of("ADMIN"));
		unitySiteWebClient.removeAdmin(siteId, userId);

		//then
		verify(userService, times(1)).getRoleValues(Mockito.eq(userId), Mockito.eq(groupPath), Mockito.eq(SITE_ADMIN));
		verify(userService, times(1)).removeUserRole(eq(userId), eq(groupPath), eq(SITE_ADMIN));
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminRole() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		String groupPath = "/fenix/sites/"+ siteId +"/users";

		//when
		when(userService.getRoleValues(eq(userId), eq(groupPath), eq(SITE_ADMIN))).thenReturn(Set.of(SITE_ADMIN.unityRoleValue, SITE_SUPPORT.unityRoleValue));
		unitySiteWebClient.removeAdmin(siteId, userId);

		//then
		verify(userService, times(1)).removeUserRole(eq(userId), eq(groupPath), eq(SITE_ADMIN));
		verify(userService, times(0)).removeUserFromGroup(eq(userId), eq(groupPath));
	}
}