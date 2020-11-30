/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class ResourceCredit {
	
	final Integer amount;
	final String unit;

	ResourceCredit(Integer amount, String unit) {
		this.amount = amount;
		this.unit = unit;
	}
}
