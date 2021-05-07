/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.imunity.furms.core.utils.ResourceCreditsUtils.includedFullyDistributedFilter;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;

class ResourceCreditsUtilsTest {

	@ParameterizedTest
	@MethodSource("includedFullyDistributedFilterTestParams")
	void includedFullyDistributedFilterTest(BigDecimal availableAmount, boolean fullyDistributed, boolean expected) {
		assertThat(includedFullyDistributedFilter(availableAmount, fullyDistributed)).isEqualTo(expected);
	}

	static Stream<Arguments> includedFullyDistributedFilterTestParams() {
		final boolean includeFullyDistributed = true;
		final boolean notIncludeFullyDistributed = false;
		return Stream.of(
				Arguments.of(null, includeFullyDistributed, true),
				Arguments.of(null, notIncludeFullyDistributed, true),
				Arguments.of(ZERO, includeFullyDistributed, true),
				Arguments.of(ZERO, notIncludeFullyDistributed, false),
				Arguments.of(ONE, includeFullyDistributed, true),
				Arguments.of(ONE, notIncludeFullyDistributed, true)
		);
	}

}