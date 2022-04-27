/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.authz.CapabilityCollector;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.core.invitations.InvitatoryService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.spi.communites.CommunityGroupsDAO;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class CommunityEventsITTest {
	@MockBean
	private CommunityGroupsDAO communityGroupsDAO;
	@MockBean
	private InvitatoryService invitatoryService;
	@MockBean
	private ProjectRepository projectRepository;

	@MockBean
	private CommunityRepository communityRepository;
	@MockBean
	private ServiceMock serviceMock;
	@MockBean
	private AuthzService authzService;
	@MockBean
	private CapabilityCollector capabilityCollector;

	@Autowired
	private CommunityService communityService;

	@Test
	void shouldRunCommunityRemoveEvent() {
		CommunityId id = new CommunityId(UUID.randomUUID());
		when(communityRepository.exists(id)).thenReturn(true);
		when(communityRepository.findById(id)).thenReturn(Optional.of(Community.builder().id(id).build()));

		communityService.delete(id);

		verify(serviceMock, times(1)).handleEventCommunityRemove();
	}

	@Test
	void shouldRunCommunityCreateEvent() {
		CommunityId id = new CommunityId(UUID.randomUUID());
		Community request = Community.builder()
			.id(id)
			.name("userFacingName")
			.build();
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.findById(id)).thenReturn(Optional.of(request));

		when(communityRepository.create(request)).thenReturn(id);

		communityService.create(request);

		verify(serviceMock, times(1)).handleEventCommunityCreate();
	}

	@Test
	void shouldRunCommunityUpdateEvent() {
		CommunityId id = new CommunityId(UUID.randomUUID());
		Community request = Community.builder()
			.id(id)
			.name("userFacingName")
			.build();
		when(communityRepository.exists(request.getId())).thenReturn(true);
		when(communityRepository.isUniqueName(request.getName())).thenReturn(true);
		when(communityRepository.findById(id)).thenReturn(Optional.of(Community.builder().build()));

		communityService.update(request);

		verify(serviceMock, times(1)).handleEventCommunityUpdate();
	}
}