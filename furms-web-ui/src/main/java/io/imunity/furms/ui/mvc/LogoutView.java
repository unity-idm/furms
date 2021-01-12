/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.mvc;

import io.imunity.furms.ui.config.FurmsI18NProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import static io.imunity.furms.domain.constant.RoutesConst.LOGOUT_URL;

@Controller
public class LogoutView {

	private final FurmsI18NProvider i18nProvider;

	LogoutView(FurmsI18NProvider i18nProvider) {
		this.i18nProvider = i18nProvider;
	}

	@RequestMapping(LOGOUT_URL)
	public String logout(Model model) {
		model.addAttribute("title", i18nProvider.getTranslation("view.logout-page.title"));
		model.addAttribute("message", i18nProvider.getTranslation("view.logout-page.message"));
		model.addAttribute("login", i18nProvider.getTranslation("view.logout-page.login"));
		return "logout";
	}
}