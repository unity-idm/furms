/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.domain.communities.CreateCommunityEvent;
import io.imunity.furms.domain.communities.RemoveCommunityEvent;
import io.imunity.furms.domain.communities.UpdateCommunityEvent;
import io.imunity.furms.domain.users.InviteUserEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ListenerMock {
	private final ServiceMock serviceMock;

	public ListenerMock(ServiceMock serviceMock) {
		this.serviceMock = serviceMock;
	}

	@EventListener
	public void handleUserEvents(InviteUserEvent event) {
		serviceMock.handleEventUserAction();
	}

	@EventListener
	public void onCommunityEvents(CreateCommunityEvent event) {
			serviceMock.handleEventCommunityCreate();
	}

	@EventListener
	public void onCommunityEvents(UpdateCommunityEvent event) {
		serviceMock.handleEventCommunityUpdate();
	}

	@EventListener
	public void onCommunityEvents(RemoveCommunityEvent event) {
		serviceMock.handleEventCommunityRemove();
	}
}
