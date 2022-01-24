/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import java.util.UUID;

class DataLoaderUtils {

	static String randomName() {
		return UUID.randomUUID().toString().substring(9, 31);
	}

	static String randomAcronym() {
		return UUID.randomUUID().toString().substring(9, 15);
	}

}
