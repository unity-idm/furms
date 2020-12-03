/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.admin;

import java.math.BigDecimal;

class ResourceAmount {
	
	final BigDecimal amount;
	final String unit;

	ResourceAmount(BigDecimal amount, String unit) {
		this.amount = amount;
		this.unit = unit;
	}
}
