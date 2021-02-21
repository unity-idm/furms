/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.error;

import io.imunity.furms.domain.users.UnknownUserException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/exception/handler")
class ExceptionHandlersStubController {

	@GetMapping("/unknown-user-exception")
	void unknownUserExceptionEndpoint() {
		throw new UnknownUserException("testUserId");
	}

	@GetMapping("/runtime-exception")
	void runtimeExceptionEndpoint() {
		throw new RuntimeException();
	}

	@GetMapping("/exception")
	void exceptionEndpoint() throws Exception {
		throw new Exception();
	}
}

