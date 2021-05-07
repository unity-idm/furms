/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import java.math.BigDecimal;

import static java.math.BigDecimal.ZERO;

public class ResourceCreditsUtils {

	public static boolean includedFullyDistributedFilter(BigDecimal availableAmount, boolean includeFullyDistributed) {
		return availableAmount == null
				|| availableAmount.compareTo(ZERO) != 0
				|| (availableAmount.compareTo(ZERO) == 0) == includeFullyDistributed;
	}

}
