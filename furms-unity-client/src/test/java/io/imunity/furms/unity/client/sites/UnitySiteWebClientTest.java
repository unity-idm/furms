/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.sites;

import static io.imunity.furms.domain.authz.roles.Role.SITE_ADMIN;
import static io.imunity.furms.unity.client.common.UnityConst.IDENTITY_TYPE;
import static io.imunity.furms.unity.client.common.UnityConst.PERSISTENT_IDENTITY;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import io.imunity.furms.domain.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.exceptions.UnityFailureException;
import io.imunity.furms.unity.client.unity.UnityClient;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.authn.CredentialInfo;
import pl.edu.icm.unity.types.basic.Attribute;
import pl.edu.icm.unity.types.basic.AttributeExt;
import pl.edu.icm.unity.types.basic.Entity;
import pl.edu.icm.unity.types.basic.EntityInformation;
import pl.edu.icm.unity.types.basic.Group;
import pl.edu.icm.unity.types.basic.GroupMember;
import pl.edu.icm.unity.types.basic.Identity;

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

	@SuppressWarnings("unchecked")
	@Test
	void shouldGetSiteAdministrators() {
		//given
		String id = UUID.randomUUID().toString();
		when(unityClient.get(contains(id), any(ParameterizedTypeReference.class)))
				.thenReturn(List.of(createGroupMember(id, 1, true),
						createGroupMember(id, 2, false),
						createGroupMember(id, 3, true)));

		//when
		List<User> admins = unitySiteWebClient.getAllAdmins(id);

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
		String groupPath = "/group/%2Ffenix%2Fsites%2F"+siteId+"%2Fusers/entity/"+userId;
		String attributePath = "/entity/"+userId+"/attribute";
		Attribute attribute = new Attribute(SITE_ADMIN.unityRoleAttribute, "enumeration",
				"/fenix/sites/"+siteId+"/users", List.of(SITE_ADMIN.unityRoleValue));

		//when
		unitySiteWebClient.addAdmin(siteId, userId);

		//then
		verify(unityClient, times(1)).post(eq(groupPath), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
		verify(unityClient, times(1)).put(eq(attributePath), eq(attribute), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
	}

	@Test
	void shouldRemoveAdmin() {
		//given
		String siteId = "siteId";
		String userId = "userId";
		String groupPath = "/group/%2Ffenix%2Fsites%2F"+siteId+"%2Fusers/entity/"+userId;

		//when
		unitySiteWebClient.removeAdmin(siteId, userId);

		//then
		verify(unityClient, times(1)).delete(eq(groupPath), eq(Map.of(IDENTITY_TYPE, PERSISTENT_IDENTITY)));
	}

	private GroupMember createGroupMember(String id, long entityId, boolean isAdmmin) {
		List<AttributeExt> attributes = new ArrayList<>();
		attributes.add(new AttributeExt(new Attribute("email", "", id,
				List.of("{\"value\":\"test@domain.com\",\"confirmationData\":{\"confirmed\":true," +
						"\"confirmationDate\":1611875237898,\"sentRequestAmount\":0},\"tags\":[]}")), true));
		if (isAdmmin) {
			attributes.add(new AttributeExt(new Attribute(
					SITE_ADMIN.unityRoleAttribute, "", id, List.of(SITE_ADMIN.unityRoleValue)), true));
		}
		return new GroupMember(id,
				new Entity(List.of(new Identity(PERSISTENT_IDENTITY, "", entityId, String.valueOf(entityId))),
						new EntityInformation(entityId), new CredentialInfo("string", Map.of())),
				attributes);
	}

}