/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static io.imunity.furms.ui.constant.LoginFlowConst.LOGIN_URL;

@Controller
public class BaseRedirectController {
	@RequestMapping("/")
	public String redirect() {
		return "forward:" + LOGIN_URL;
	}
}
