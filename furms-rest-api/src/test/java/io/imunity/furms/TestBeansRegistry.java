/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms;

import org.springframework.boot.test.mock.mockito.MockBean;

import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.spi.roles.RoleLoader;

public class TestBeansRegistry {

	@MockBean
	protected UserService userService;

	@MockBean
	protected UserApiKeyService userApiKeyService;

	@MockBean
	protected RoleLoader roleLoader;

}
