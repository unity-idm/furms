/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.communities.CommunityEvent;
import io.imunity.furms.domain.projects.ProjectEvent;
import io.imunity.furms.domain.sites.SiteEvent;
import io.imunity.furms.domain.users.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
import java.util.List;

@Component
public class VaadinBroadcaster {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	private final List<Runnable> listeners = new LinkedList<>();

	public synchronized Registration register(Runnable listener) {
		listeners.add(listener);
		return new SynchronizedRegistration(() -> listeners.remove(listener));
	}

	private synchronized void broadcast() {
		for (Runnable listener : listeners) {
			try {
				listener.run();
				LOG.info("Run Runnable");
			}catch (Exception e){
				LOG.error("Runnable failed", e);
			}
		}
	}

	@Async
	@EventListener
	void onUserEvents(UserEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void onProjectEvents(ProjectEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void onCommunityEvents(CommunityEvent event) {
		broadcast();
	}

	@Async
	@EventListener
	void onSiteEvents(SiteEvent event) {
		broadcast();
	}
}
