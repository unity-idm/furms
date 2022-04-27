/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
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
import static org.mockito.quality.Strictness.LENIENT;

@ExtendWith(MockitoExtension.class)
class UnitySiteGroupDAOTest {

	@Mock
	private UnityClient unityClient;

	@Mock
	private UserService userService;

	@InjectMocks
	private UnitySiteGroupDAO unitySiteWebClient;

	@Test
	void shouldGetMetaInfoAboutSite() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		Group group = new Group("/path/"+id);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(id.id.toString()), eq(Group.class))).thenReturn(group);

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
				.id(new SiteId(UUID.randomUUID()))
				.name("test")
				.build();

		//when
		unitySiteWebClient.create(site);

		//then
		verify(unityClient, times(1)).post(anyString(), any());
		verify(unityClient, times(1)).post(anyString());
	}

	@SuppressWarnings("unchecked")
	@Test
	@MockitoSettings(strictness = LENIENT)
	void shouldExpectExceptionWhenCommunicationWithUnityIsBroken() {
		//given
		WebClientResponseException webException = new WebClientResponseException(400, "BAD_REQUEST", null, null, null);
		doThrow(webException).when(unityClient).get(anyString(), any(Class.class));
		doThrow(webException).when(unityClient).post(anyString());
		doThrow(webException).when(unityClient).delete(anyString(), any());

		//when + then
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.get(new SiteId(UUID.randomUUID())));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.create(Site.builder().id(new SiteId(UUID.randomUUID())).build()));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.update(Site.builder().id(new SiteId(UUID.randomUUID())).build()));
		assertThrows(UnityFailureException.class, () -> unitySiteWebClient.delete(new SiteId(UUID.randomUUID())));
	}

	@Test
	void shouldUpdateSite() {
		//given
		Site site = Site.builder()
				.id(new SiteId(UUID.randomUUID()))
				.name("test")
				.build();
		Group group = new Group("/path/"+site.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(site.getId().id.toString()), eq(Group.class))).thenReturn(group);

		//when
		unitySiteWebClient.update(site);

		//then
		verify(unityClient, times(1)).get(eq("/group/%2Ffenix%2Fsites%2F" + site.getId().id + "/meta"), eq(Group.class));
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveSite() {
		//given
		SiteId id = new SiteId(UUID.randomUUID());
		doNothing().when(unityClient).delete(contains(id.id.toString()), anyMap());

		//when
		unitySiteWebClient.delete(id);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

	@Test
	void shouldGetSiteAdministrators() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		String groupPath = "/fenix/sites/"+ siteId.id +"/users";
		when(userService.getAllUsersByRoles(groupPath, Set.of(SITE_ADMIN)))
			.thenReturn(List.of(
				FURMSUser.builder()
					.id(new PersistentId("1"))
					.firstName("firstName")
					.lastName("lastName")
					.email("email")
					.build(),
				FURMSUser.builder()
					.id(new PersistentId("3"))
					.firstName("firstName")
					.lastName("lastName")
					.email("email")
					.build()
				)
			);

		//when
		List<FURMSUser> admins = unitySiteWebClient.getSiteUsers(siteId, Set.of(SITE_ADMIN));

		//then
		assertThat(admins).hasSize(2);
		assertThat(admins.stream()
				.allMatch(user -> user.id.get().id.equals("1") || user.id.get().id.equals("3"))).isTrue();
	}

	@Test
	void shouldAddAdminToSite() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		String groupPath = "/fenix/sites/"+ siteId.id +"/users";
		//when
		unitySiteWebClient.addSiteUser(siteId, userId, SITE_ADMIN);

		//then
		verify(userService, times(1)).addUserRole(eq(userId), eq(groupPath), eq(SITE_ADMIN));
		verify(userService, times(1)).addUserToGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveSiteRoleFromGroup() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		String groupPath = "/fenix/sites/"+ siteId.id +"/users";

		//when
		unitySiteWebClient.removeSiteUser(siteId, userId);

		//then
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminRole() {
		//given
		SiteId siteId = new SiteId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		String groupPath = "/fenix/sites/"+ siteId.id +"/users";

		unitySiteWebClient.removeSiteUser(siteId, userId);

		//then
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
	}
}