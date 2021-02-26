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
import java.util.function.Consumer;

@Component
public class VaadinBroadcaster {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final List<Consumer<FurmsEvent>> listeners = new ArrayList<>();

	public Registration register(Consumer<FurmsEvent> listener) {
		addListener(listener);
		return () -> removeListener(listener);
	}

	private synchronized void addListener(Consumer<FurmsEvent> listener){
		listeners.add(listener);
	}

	private synchronized void removeListener(Consumer<FurmsEvent> listener){
		listeners.remove(listener);
	}

	private void broadcast(FurmsEvent event) {
		List<Consumer<FurmsEvent>> listenersToBeNotified = getListenersToBeNotified();
		notifyListeners(listenersToBeNotified, event);
	}

	private synchronized List<Consumer<FurmsEvent>> getListenersToBeNotified(){
		return List.copyOf(listeners);
	}

	private void notifyListeners(List<Consumer<FurmsEvent>> listeners, FurmsEvent event){
		for (Consumer<FurmsEvent> listener : listeners) {
			try {
				listener.accept(event);
				LOG.info("Run Runnable");
			} catch (Exception e) {
				LOG.error("Runnable failed", e);
			}
		}
	}

	@Async
	@EventListener
	void onFurmsEvents(FurmsEvent event) {
		broadcast(event);
	}
}
