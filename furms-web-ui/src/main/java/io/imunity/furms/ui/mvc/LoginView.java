/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.mvc;

import io.imunity.furms.ui.config.FurmsI18NProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_ERROR_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_URL;
import static io.imunity.furms.domain.constant.RoutesConst.PROXY_AUTH_PARAM;


@Controller
public class LoginView {
	
	private final FurmsI18NProvider i18nProvider;
	private final RedirectService redirectService;

	LoginView(FurmsI18NProvider i18nProvider, RedirectService redirectService) {
		this.i18nProvider = i18nProvider;
		this.redirectService = redirectService;
	}

	@GetMapping(path = {"/", LOGIN_URL})
	public String redirectToAuthN(@RequestParam Map<String, String> params) {
		return "redirect:" + redirectService.getRedirectURL(params.containsKey(PROXY_AUTH_PARAM));
	}
	
	@GetMapping(LOGIN_ERROR_URL)
	public String loginError(Model model) {
		model.addAttribute("title", i18nProvider.getTranslation("view.error-page.login.title"));
		model.addAttribute("message", i18nProvider.getTranslation("view.error-page.login.message.auth"));
		return "login-error";
	}
}
