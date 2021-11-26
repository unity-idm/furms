/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;

import java.lang.invoke.MethodHandles;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import com.vaadin.componentfactory.IdleNotification;
import com.vaadin.flow.server.ServiceException;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.SessionInitEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedHttpSession;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.annotation.EnableVaadin;

import io.imunity.furms.domain.constant.RoutesConst;

@Configuration
@EnableVaadin("io.imunity.furms")
class VaadinConfiguration {
		
	@Bean
	public ServletRegistrationBean<SpringServlet> configVaadinMapping(ApplicationContext context,
	                                                                  FrontProperties frontConfig,
	                                                                  FurmsI18NProvider i18nProvider,
	                                                                  CustomCSSProvider customCSSProvider) {
		return new ServletRegistrationBean<>(new FurmsVaadinServlet(context, false, frontConfig, 
				i18nProvider, customCSSProvider), FRONT + "/*");
	}
	
	private static class FurmsVaadinServlet extends SpringServlet {
		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		private final FrontProperties frontConfig;
		private final FurmsI18NProvider i18nProvider;
		private final CustomCSSProvider customCSSProvider;

		public FurmsVaadinServlet(ApplicationContext context,
		                          boolean rootMapping,
		                          FrontProperties frontConfig,
		                          FurmsI18NProvider i18nProvider,
		                          CustomCSSProvider customCSSProvider) {
			super(context, rootMapping);
			this.frontConfig = frontConfig;
			this.i18nProvider = i18nProvider;
			this.customCSSProvider = customCSSProvider;
		}

		@Override
		protected void servletInitialized() throws ServletException {
			super.servletInitialized();
			getService().addSessionInitListener(this::sessionInit);
			getService().addSessionDestroyListener(this::sessionDestroy);
			getService().addUIInitListener(this::onUIInit);
		}
		
		private void onUIInit(UIInitEvent initEvent) {
			
			LOG.debug("A new UI has been initialized, warning will be shown {}s before expiration", 
					frontConfig.getSecondsBeforeShowingSessionExpirationWarning());
			IdleNotification idleNotification = new IdleNotification(
					frontConfig.getSecondsBeforeShowingSessionExpirationWarning());
			String warning = i18nProvider.getTranslation("sessionExpiration.expirationWarning", 
					IdleNotification.MessageFormatting.SECS_TO_TIMEOUT);
			idleNotification.setMessage(warning);
			idleNotification.addExtendSessionButton(i18nProvider.getTranslation("sessionExpiration.extend"));
			idleNotification.addRedirectButton(i18nProvider.getTranslation("sessionExpiration.logoutNow"), 
					RoutesConst.LOGOUT_TRIGGER_URL);
			idleNotification.setRedirectAtTimeoutUrl(RoutesConst.LOGOUT_TRIGGER_URL);
			idleNotification.addCloseButton();
			idleNotification.setExtendSessionOnOutsideClick(false);
			initEvent.getUI().add(idleNotification);

			customCSSProvider.initAndAttach(initEvent.getUI());

			WrappedSession wrappedSession = VaadinSession.getCurrent().getSession();
			String sessionId = wrappedSession.getId();
			initEvent.getUI().addDetachListener(event -> {
				LOG.debug("Closing UI of session {}", sessionId);
			});

			UIInSessionHolder.addUIToSession(initEvent.getUI(), (WrappedHttpSession) wrappedSession);
			LOG.debug("Saved UI in session {}", sessionId);
		}
		
		private void sessionDestroy(SessionDestroyEvent event) {
			VaadinServletRequest request = (VaadinServletRequest) VaadinRequest.getCurrent();
			if (request != null) {
				WrappedSession wrappedSession = request.getWrappedSession(false);
				if (wrappedSession != null) {
					LOG.debug("Triggering spring logout from Vaadin session destroy: {}", 
							wrappedSession.getId());
					new SecurityContextLogoutHandler().logout(request, null, null);
				}
			}
		}

		private void sessionInit(SessionInitEvent event) throws ServiceException {
			WrappedSession wrappedSession = event.getSession().getSession();
			wrappedSession.setMaxInactiveInterval(frontConfig.getMaxSessionInactivity());
			LOG.debug("Session {} created, max inactivity set to {}",
					wrappedSession.getId(), 
					wrappedSession.getMaxInactiveInterval());
		}
	}
}
