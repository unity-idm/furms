/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.ui.config;

import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.shared.communication.PushMode;

@WebListener
@Component
public class CustomSessionListener implements HttpSessionListener {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void sessionCreated(HttpSessionEvent se) {
		LOG.debug("New session is created {}", se.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		LOG.debug("HTTP session destroyed {}", se.getSession().getId());
		List<UI> uis = UIInSessionHolder.getUIsFromSession(se.getSession());
		LOG.debug("Disabling push in {} UIs", uis.size());
		for (UI ui: uis) {
			try {
				ui.getPushConfiguration().setPushMode(PushMode.DISABLED);
			} catch (Exception e) {
				LOG.debug("Unable to disable push for UI", e);
			}
			ui.getInternals().setPushConnection(null);
		}
	}
}