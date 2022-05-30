/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.communities;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.unity.client.UnityClient;
import io.imunity.furms.unity.client.users.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.domain.authz.roles.Role.COMMUNITY_ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UnityCommunityGroupsDAOTest {

	@Mock
	private UnityClient unityClient;

	@Mock
	private UserService userService;

	@InjectMocks
	private UnityCommunityGroupsDAO unityCommunityWebClient;

	@Test
	void shouldGetMetaInfoAboutCommunity() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());
		Group group = new Group("/path/"+id);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(id.id.toString()), eq(Group.class))).thenReturn(group);

		//when
		Optional<CommunityGroup> community = unityCommunityWebClient.get(id);

		//then
		assertThat(community).isPresent();
		assertThat(community.get().getId()).isEqualTo(id);
		assertThat(community.get().getName()).isEqualTo("test");
	}

	@Test
	void shouldCreateCommunity() {
		//given
		CommunityGroup community = CommunityGroup.builder()
				.id(new CommunityId(UUID.randomUUID()))
				.name("test")
				.build();
		doNothing().when(unityClient).post(contains(community.getId().id.toString()), any());
		doNothing().when(unityClient).post(contains("users"), any());

		//when
		unityCommunityWebClient.create(community);

		//then
		verify(unityClient, times(1)).post(anyString(), any());
		verify(unityClient, times(1)).post(anyString());
	}

	@Test
	void shouldUpdateCommunity() {
		//given
		CommunityGroup community = CommunityGroup.builder()
				.id(new CommunityId(UUID.randomUUID()))
				.name("test")
				.build();
		Group group = new Group("/path/"+community.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(community.getId().id.toString()), eq(Group.class))).thenReturn(group);
		doNothing().when(unityClient).put(contains(community.getId().id.toString()), eq(Group.class));

		//when
		unityCommunityWebClient.update(community);

		//then
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveCommunity() {
		//given
		CommunityId id = new CommunityId(UUID.randomUUID());
		doNothing().when(unityClient).delete(contains(id.id.toString()), anyMap());

		//when
		unityCommunityWebClient.delete(id);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

	@Test
	void shouldGetCommunityAdministrators() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		String groupPath = "/fenix/communities/"+ communityId.id +"/users";
		when(userService.getAllUsersByRole(groupPath, COMMUNITY_ADMIN))
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
			));

		//when
		List<FURMSUser> admins = unityCommunityWebClient.getAllAdmins(communityId);

		//then
		assertThat(admins).hasSize(2);
		assertThat(admins.stream()
			.allMatch(user -> user.id.get().id.equals("1") || user.id.get().id.equals("3"))).isTrue();
	}

	@Test
	void shouldAddAdminToCommunity() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		String groupPath = "/fenix/communities/"+ communityId.id +"/users";
		//when
		unityCommunityWebClient.addAdmin(communityId, userId);

		//then
		verify(userService, times(1)).addUserRole(eq(userId), eq(groupPath), eq(COMMUNITY_ADMIN));
		verify(userService, times(1)).addUserToGroup(eq(userId), eq(groupPath));
	}

	@Test
	void shouldRemoveAdminRole() {
		//given
		CommunityId communityId = new CommunityId(UUID.randomUUID());
		PersistentId userId = new PersistentId("userId");
		String groupPath = "/fenix/communities/"+ communityId.id +"/users";

		//when
		unityCommunityWebClient.removeAdmin(communityId, userId);

		//then
		verify(userService, times(1)).removeUserFromGroup(eq(userId), eq(groupPath));
		verify(userService, times(0)).removeUserRole(eq(userId), eq(groupPath), eq(COMMUNITY_ADMIN));
	}
}