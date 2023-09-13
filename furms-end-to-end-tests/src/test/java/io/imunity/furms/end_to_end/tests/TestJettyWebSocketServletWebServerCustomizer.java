/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.core.server.WebSocketMappings;
import org.eclipse.jetty.websocket.core.server.WebSocketServerComponents;
import org.eclipse.jetty.websocket.javax.server.internal.JavaxWebSocketServerContainer;
import org.eclipse.jetty.websocket.server.JettyWebSocketServerContainer;
import org.eclipse.jetty.websocket.servlet.WebSocketUpgradeFilter;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
class TestJettyWebSocketServletWebServerCustomizer implements WebServerFactoryCustomizer<JettyServletWebServerFactory>, Ordered
{

	@Override
	public void customize(JettyServletWebServerFactory factory) {
		factory.addConfigurations(new AbstractConfiguration() {
			@Override
			public void configure(WebAppContext context) {
				ContextHandler.Context servletContext = context.getServletContext();
				Server server = context.getServer();
				WebSocketServerComponents.ensureWebSocketComponents(server, servletContext);
				JettyWebSocketServerContainer.ensureContainer(servletContext);
				WebSocketServerComponents.ensureWebSocketComponents(server, servletContext);
				WebSocketUpgradeFilter.ensureFilter(servletContext);
				WebSocketMappings.ensureMappings(servletContext);
				JavaxWebSocketServerContainer.ensureContainer(servletContext);
			}

		});
	}

	@Override
	public int getOrder() {
		return 0;
	}
}
