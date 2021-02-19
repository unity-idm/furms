/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import io.imunity.furms.utils.EventOperation;

public class CommunityEvent {
	public final String id;
	public final EventOperation operation;

	public CommunityEvent(String id, EventOperation operation) {
		this.id = id;
		this.operation = operation;
	}
}
