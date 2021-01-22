/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.projects;

import io.imunity.furms.domain.projects.ProjectGroup;
import io.imunity.furms.unity.client.unity.UnityClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.edu.icm.unity.types.I18nString;
import pl.edu.icm.unity.types.basic.Group;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UnityProjectGroupsDAOTest {

	@Mock
	private UnityClient unityClient;

	@InjectMocks
	private UnityProjectGroupsDAO unityProjectGroupsDAO;

	@Test
	void shouldGetMetaInfoAboutProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		Group group = new Group("/path/" + communityId + "/projects/" + projectId);
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(projectId), eq(Group.class))).thenReturn(group);

		//when
		Optional<ProjectGroup> project = unityProjectGroupsDAO.get(communityId, projectId);

		//then
		assertThat(project).isPresent();
		assertThat(project.get().getId()).isEqualTo(projectId);
		assertThat(project.get().getName()).isEqualTo("test");
		assertThat(project.get().getCommunityId()).isEqualTo(communityId);
	}

	@Test
	void shouldCreateCommunity() {
		//given
		ProjectGroup project = ProjectGroup.builder()
			.id(UUID.randomUUID().toString())
			.communityId(UUID.randomUUID().toString())
			.name("test")
			.build();
		doNothing().when(unityClient).post(contains(project.getId()), any());
		doNothing().when(unityClient).post(contains(project.getCommunityId()), any());
		doNothing().when(unityClient).post(contains("users"), any());

		//when
		unityProjectGroupsDAO.create(project);

		//then
		verify(unityClient, times(1)).post(anyString(), any(), any());
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldUpdateCommunity() {
		//given
		ProjectGroup project = ProjectGroup.builder()
				.id(UUID.randomUUID().toString())
				.communityId(UUID.randomUUID().toString())
				.name("test")
				.build();
		Group group = new Group("/path/"+project.getId());
		group.setDisplayedName(new I18nString("test"));
		when(unityClient.get(contains(project.getId()), eq(Group.class))).thenReturn(group);
		doNothing().when(unityClient).put(contains(project.getId()), eq(Group.class));

		//when
		unityProjectGroupsDAO.update(project);

		//then
		verify(unityClient, times(1)).put(anyString(), any());
	}

	@Test
	void shouldRemoveProject() {
		//given
		String communityId = UUID.randomUUID().toString();
		String projectId = UUID.randomUUID().toString();
		doNothing().when(unityClient).delete(contains(projectId), anyMap());

		//when
		unityProjectGroupsDAO.delete(communityId, projectId);

		//then
		verify(unityClient, times(1)).delete(anyString(), any());
	}

}