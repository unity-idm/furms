/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.utils;

public enum MDCKey {
	QUEUE_NAME("queueName"), 
	USER_ID("userId");

	public final String key;

	MDCKey(String key) {
		this.key = key;
	}
}
