/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db;

import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootApplication
public class TestSpringContextConfig {
	@MockBean
	UsersDAO usersDAO;
}
