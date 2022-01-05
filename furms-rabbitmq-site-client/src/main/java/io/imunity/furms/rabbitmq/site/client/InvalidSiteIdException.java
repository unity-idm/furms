/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

class InvalidSiteIdException extends RuntimeException {
	InvalidSiteIdException(String message) {
		super(message);
	}
}
