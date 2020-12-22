/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static io.imunity.furms.unity.client.unity.UriVariableUtils.buildPath;
import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UriVariableUtilsTest {

	@Test
	void shouldThrowExceptionForEmptyPath() {
		assertThrows(IllegalArgumentException.class, () -> buildPath(null, null));
		assertThrows(IllegalArgumentException.class, () -> buildPath(EMPTY_STRING, null));
	}

	@ParameterizedTest
	@MethodSource(value = "parametersForShouldReplaceMatchedVariables")
	void shouldReplaceMatchedVariables(String path, String expectedValue, Map<String, Object> params) {
		assertThat(buildPath(path, params)).isEqualTo(expectedValue);
	}

	static Stream<Arguments> parametersForShouldReplaceMatchedVariables() {
		return Stream.of(
				Arguments.of("/path/{variable}/{variable2}", "/path/test/test2",
						Map.of("variable", "test", "variable2", "test2")),
				Arguments.of("/path/{variable}/{variable}", "/path/test/test",
						Map.of("variable", "test")),
				Arguments.of("/path/{variable}/{unknown_variable}", "/path/test/",
						Map.of("variable", "test", "variable2", "test2")),
				Arguments.of("/path/{numberVariable}", "/path/1111",
						Map.of("numberVariable", "1111")),
				Arguments.of("/path/{variable}", "/path/",
						null)

		);
	}

}