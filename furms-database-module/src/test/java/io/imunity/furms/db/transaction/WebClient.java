/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.transaction;

import io.imunity.furms.domain.communities.Community;
import org.springframework.stereotype.Service;

@Service
class WebClient {
	void update(Community community){
		throw new RuntimeException();
	}
}
