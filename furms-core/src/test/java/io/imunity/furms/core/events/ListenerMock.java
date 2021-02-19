/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.domain.communities.CommunityEvent;
import io.imunity.furms.domain.users.UserEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static io.imunity.furms.utils.EventOperation.*;

@Component
public class ListenerMock {
	private final ServiceMock serviceMock;

	public ListenerMock(ServiceMock serviceMock) {
		this.serviceMock = serviceMock;
	}

	@Async
	@EventListener
	public void handleUserEvents(UserEvent event) {
		serviceMock.doEventUserAction();
	}

	@Async
	@EventListener
	public void handleCommunityEvents(CommunityEvent event) {
		if(event.operation.equals(CREATE))
			serviceMock.doEventCommunityCreate();
		if(event.operation.equals(UPDATE))
			serviceMock.doEventCommunityUpdate();
		if(event.operation.equals(DELETE))
			serviceMock.doEventCommunityRemove();
	}
}
