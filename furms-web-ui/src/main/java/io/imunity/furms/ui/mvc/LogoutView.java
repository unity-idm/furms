/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.mvc;

import static io.imunity.furms.domain.constant.RoutesConst.POST_LOGOUT_PAGE_URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import io.imunity.furms.api.authz.SessionDisposer;
import io.imunity.furms.ui.config.FurmsI18NProvider;

@Controller
public class LogoutView {

	private static final int DELAY_TO_DISPOSE_SESSION = 1000;
	private final FurmsI18NProvider i18nProvider;
	private final SessionDisposer sessionDisposer;

	LogoutView(FurmsI18NProvider i18nProvider, SessionDisposer sessionDisposer) {
		this.i18nProvider = i18nProvider;
		this.sessionDisposer = sessionDisposer;
	}

	@RequestMapping(POST_LOGOUT_PAGE_URL)
	public String logout(Model model, HttpServletRequest request) {		
		model.addAttribute("title", i18nProvider.getTranslation("view.logout-page.title"));
		model.addAttribute("message", i18nProvider.getTranslation("view.logout-page.message"));
		model.addAttribute("login", i18nProvider.getTranslation("view.logout-page.login"));
		
		HttpSession session = request.getSession();
		new Thread(() -> killSessionAfterWhile(session)).start();
		return "logout";
	}

	//That's ugly hack, to prevent vaadin to push session expiration to a live UI, what causes this UI to refresh itself
	// what in turn cancels ongoing request (logout one). I.e. without that vaadin would stop loading logout page and 
	//instead refresh its current UI.
	//TODO: try to find a better way and to stop Vaadin to push this event (or to reload current page in effect of that event).
	private void killSessionAfterWhile(HttpSession request)
	{
		try {
			Thread.sleep(DELAY_TO_DISPOSE_SESSION);
		} catch (InterruptedException e) {}
		sessionDisposer.invalidateSession(request);
	}
}