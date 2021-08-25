/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.api.users.UserService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
class CentralIdpRestAPIConfiguration {

	@Bean
	CentralIdPRestAPIService centralIdPRestAPIService(UserService userService) {
		return new CentralIdPRestAPIService(userService);
	}

}
