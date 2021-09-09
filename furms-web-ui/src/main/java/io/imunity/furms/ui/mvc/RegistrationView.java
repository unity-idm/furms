/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.mvc;

import io.imunity.furms.api.invitations.InvitationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.invoke.MethodHandles;
import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.ROLE_CHOOSER_URL;

@Controller
class RegistrationView {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final InvitationService invitationService;

	RegistrationView(InvitationService invitationService) {
		this.invitationService = invitationService;
	}

	@GetMapping(path = "/public/registration")
	public String removeInvitationAndRedirect(@RequestParam Map<String, String> params) {
		String requestId = params.get("request_id");
		if(requestId != null)
			invitationService.deleteBy(requestId);
		else
			LOG.warn("No unity code to removing invitation");
		return "redirect:" + "/front/" + ROLE_CHOOSER_URL;
	}
}
