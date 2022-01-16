/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.domain.communities.CommunityCreatedEvent;
import io.imunity.furms.domain.communities.CommunityRemovedEvent;
import io.imunity.furms.domain.communities.CommunityUpdatedEvent;
import io.imunity.furms.domain.users.UserEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ListenerMock {
	private final ServiceMock serviceMock;

	public ListenerMock(ServiceMock serviceMock) {
		this.serviceMock = serviceMock;
	}

	@EventListener
	public void onUserEvents(UserEvent event) {
		serviceMock.handleEventUserAction();
	}

	@EventListener
	public void onCommunityEvents(CommunityCreatedEvent event) {
			serviceMock.handleEventCommunityCreate();
	}

	@EventListener
	public void onCommunityEvents(CommunityUpdatedEvent event) {
		serviceMock.handleEventCommunityUpdate();
	}

	@EventListener
	public void onCommunityEvents(CommunityRemovedEvent event) {
		serviceMock.handleEventCommunityRemove();
	}
}
