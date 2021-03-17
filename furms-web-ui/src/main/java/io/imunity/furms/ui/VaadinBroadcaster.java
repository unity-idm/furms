/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import com.vaadin.flow.shared.Registration;
import io.imunity.furms.domain.FurmsEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Component
public class VaadinBroadcaster {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final List<VaadinListener> listeners = new ArrayList<>();

	public Registration register(VaadinListener listener) {
		addListener(listener);
		return () -> removeListener(listener);
	}

	private synchronized void addListener(VaadinListener listener){
		listeners.add(listener);
	}

	private synchronized void removeListener(VaadinListener listener){
		listeners.remove(listener);
	}

	private void broadcast(FurmsEvent event) {
		List<VaadinListener> listenersToBeNotified = getListenersToBeNotified();
		notifyListeners(listenersToBeNotified, event);
	}

	private synchronized List<VaadinListener> getListenersToBeNotified(){
		return List.copyOf(listeners);
	}

	private void notifyListeners(List<VaadinListener> listeners, FurmsEvent event){
		for (VaadinListener listener : listeners) {
			if(listener.isApplicable(event)) {
				try {
					LOG.debug("Will notify listener {} with event {}", listener, event);
					listener.run(event);
				} catch (Exception e) {
					LOG.debug("Listener listener {} with event {} failed", listener, event, e);
				}
			}
		}
	}

	@Async
	@EventListener
	void onFurmsEvents(FurmsEvent event) {
		broadcast(event);
	}
}
