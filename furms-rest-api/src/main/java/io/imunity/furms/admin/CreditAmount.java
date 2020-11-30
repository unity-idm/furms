/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

class CreditAmount {
	
	final Integer amount;
	final String unit;

	CreditAmount(Integer amount, String unit) {
		this.amount = amount;
		this.unit = unit;
	}
}
