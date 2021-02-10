/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.communities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import io.imunity.furms.domain.communities.CommunityGroup;
import io.imunity.furms.unity.client.UnityClient;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

@ExtendWith(SpringExtension.class)
class UnityCommunityGroupsDAOTest {

	@Mock
	private UnityClient unityClient;

	@InjectMocks
	private UnityCommunityGroupsDAO unityCommunityWebClient;

	@Test
	void shouldGetMetaInfoAboutCommunity() {
		//given
		String id = UUID.randomUUID().toString();
		Group group = new Group("/path/"+id);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(id), eq(Group.class))).thenReturn(group);

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
				.id(UUID.randomUUID().toString())
				.name("test")
				.build();
		doNothing().when(unityClient).post(contains(community.getId()), any());
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
				.id(UUID.randomUUID().toString())
				.name("test")
				.build();
		Group group = new Group("/path/"+community.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(community.getId()), eq(Group.class))).thenReturn(group);
		doNothing().when(unityClient).put(contains(community.getId()), eq(Group.class));

		//when
		unityCommunityWebClient.update(community);

		//then
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveCommunity() {
		//given
		String id = UUID.randomUUID().toString();
		doNothing().when(unityClient).delete(contains(id), anyMap());

		//when
		unityCommunityWebClient.delete(id);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

}