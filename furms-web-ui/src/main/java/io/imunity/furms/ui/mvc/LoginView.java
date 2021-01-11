/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.mvc;

import static io.imunity.furms.domain.constant.LoginFlowConst.AUTH_REQ_BASE_URL;
import static io.imunity.furms.domain.constant.LoginFlowConst.AUTH_REQ_PARAM_URL;
import static io.imunity.furms.domain.constant.LoginFlowConst.LOGIN_ERROR_URL;
import static io.imunity.furms.domain.constant.LoginFlowConst.LOGIN_URL;
import static io.imunity.furms.domain.constant.LoginFlowConst.PROXY_AUTH_PARAM;
import static io.imunity.furms.domain.constant.LoginFlowConst.REGISTRATION_ID;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.imunity.furms.ui.config.FurmsI18NProvider;

@Controller
public class LoginView {
	
	private final FurmsI18NProvider i18nProvider;
	
	LoginView(FurmsI18NProvider i18nProvider) {
		this.i18nProvider = i18nProvider;
	}

	@GetMapping(LOGIN_URL)
	public String redirectToAuthN(@RequestParam Map<String, String> params) {

		boolean showSignInOptions = params.containsKey(PROXY_AUTH_PARAM);
		String forwardURL = AUTH_REQ_PARAM_URL;
		if (showSignInOptions)
			forwardURL = AUTH_REQ_BASE_URL;

		return "redirect:" + forwardURL + REGISTRATION_ID;
	}
	
	@GetMapping(LOGIN_ERROR_URL)
	public String loginError(Model model) {
		model.addAttribute("title", i18nProvider.getTranslation("view.error-page.title"));
		model.addAttribute("message", i18nProvider.getTranslation("view.error-page.message"));
		return "login-error";
	}
}
