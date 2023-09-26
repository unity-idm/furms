/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.core.MockedTransactionManager;
import io.imunity.furms.core.post_commit.PostCommitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication(scanBasePackageClasses = PostCommitRunner.class)
class SpringBootLauncher
{
	@Autowired
	private PostCommitRunner postCommitRunner;

	@Bean
	MockedTransactionManager transactionManager() {
		return new MockedTransactionManager();
	}
}
