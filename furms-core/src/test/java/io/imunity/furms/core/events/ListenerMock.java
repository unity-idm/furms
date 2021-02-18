/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.events;

import io.imunity.furms.api.events.CRUD;
import io.imunity.furms.api.events.FurmsEvent;
import io.imunity.furms.api.events.UserEvent;
import io.imunity.furms.domain.communities.Community;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ListenerMock {
	private final ServiceMock serviceMock;

	public ListenerMock(ServiceMock serviceMock) {
		this.serviceMock = serviceMock;
	}

	@EventListener
	public void handleUserEvents(FurmsEvent<UserEvent> event) {
		serviceMock.doEventUserAction();
	}

	@EventListener
	public void handleCommunityEvents(FurmsEvent<Community> event) {
		if(event.crud.equals(CRUD.CREATE))
			serviceMock.doEventCommunityCreate();
		if(event.crud.equals(CRUD.UPDATE))
			serviceMock.doEventCommunityUpdate();
	}

	@EventListener
	public void handleCommunityDeleteEvents(FurmsEvent<String> event) {
		serviceMock.doEventCommunityRemove();
	}
}
