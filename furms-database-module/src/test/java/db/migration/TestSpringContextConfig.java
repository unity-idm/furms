/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package db.migration;

import io.imunity.furms.spi.users.UsersDAO;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootApplication(scanBasePackages = "io.imunity.furms.db.*")
public class TestSpringContextConfig {
	@MockBean
	UsersDAO usersDAO;
}
