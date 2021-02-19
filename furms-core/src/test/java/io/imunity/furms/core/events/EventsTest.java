/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class EventsTest {
	@MockBean
	private CommunityGroupsDAO communityGroupsDAO;
	@MockBean
	private UsersDAO usersDAO;
	@MockBean
	private ProjectRepository projectRepository;

	@MockBean
	private CommunityRepository communityRepository;
	@MockBean
	private ServiceMock serviceMock;

	@Autowired
	private CommunityService communityService;

	@Test
	void shouldRunUserChangeEvent() {
		communityService.addAdmin("id", "id");
		verify(serviceMock, timeout(100).times(1)).doEventUserAction();
	}

	@Test
	void shouldRunCommunityRemoveEvent() {
		when(communityRepository.exists("id")).thenReturn(true);

		communityService.delete("id");

		verify(serviceMock, timeout(100).times(1)).doEventCommunityRemove();
	}

	@Test
	void shouldRunCommunityCreateEvent() {
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.create(request)).thenReturn("id");

		communityService.create(request);

		verify(serviceMock, timeout(100).times(1)).doEventCommunityCreate();
	}

	@Test
	void shouldRunCommunityUpdateEvent() {
		Community request = Community.builder()
			.id("id")
			.name("userFacingName")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);

		communityService.update(request);

		verify(serviceMock, timeout(100).times(1)).doEventCommunityUpdate();
	}
}