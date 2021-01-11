/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_URL;


@Controller
class BaseRedirectController {
	@RequestMapping("/")
	public String redirect() {
		return "forward:" + LOGIN_URL;
	}
}
