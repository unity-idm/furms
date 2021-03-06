/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.server;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * App status checking (mostly devops).
 */
@RestController
@RequestMapping("/public/app-health")
class AppHealthRESTController {
	@GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
	public String getAppStatus() {
		return "OK";
	}
}
