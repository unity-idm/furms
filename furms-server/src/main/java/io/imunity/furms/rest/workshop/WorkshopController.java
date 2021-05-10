/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.workshop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rest-api/v1/workshop", produces = MediaType.APPLICATION_JSON_VALUE)
@Profile(WorkshopService.ACTIVATION_PROFILE_NAME)
public class WorkshopController {

	private final WorkshopService service;

	@Autowired
	WorkshopController(WorkshopService service) {
		this.service = service;
	}

	@PostMapping("/equipUsers")
	public void equipUsers() {
		service.equipUsers();
	}
}
