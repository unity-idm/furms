/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

public class DBIntegrationTest {
	@Autowired
	private RepoCleaner repoCleaner;
	
	@BeforeEach
	void cleanDBBefore() {
		repoCleaner.cleanAll();
	}

	@AfterEach
	void cleanDBAfter() {
		repoCleaner.cleanAll();
	}
}
