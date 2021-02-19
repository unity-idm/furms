/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.communities.CommunityEvent;
import io.imunity.furms.domain.projects.ProjectEvent;
import io.imunity.furms.domain.sites.SiteEvent;
import io.imunity.furms.domain.users.UserEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class VaadinBroadcaster {

	private static final List<Runnable> listeners = new LinkedList<>();

	public static synchronized Registration register(Runnable listener) {
		listeners.add(listener);
		return () -> {
			synchronized (VaadinBroadcaster.class) {
				listeners.remove(listener);
			}
		};
	}

	private static synchronized void broadcast() {
		for (Runnable listener : listeners) {
			listener.run();
		}
	}

	@Async
	@EventListener
	void handleUserEvents(UserEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void handleProjectEvents(ProjectEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void handleCommunityEvents(CommunityEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void handleSiteEvents(SiteEvent event) {
		broadcast();
	}
}
