/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import java.util.Map;

import static io.netty.util.internal.StringUtil.EMPTY_STRING;
import static java.lang.String.format;
import static org.springframework.util.StringUtils.isEmpty;

public class UriVariableUtils {

	private final static String VARIABLE_INDICATOR = "\\{%s\\}";
	private final static String EMPTY_VARIABLE = "\\{.*\\}";

	public static String buildPath(String path, Map<String, Object> variables) {
		if (isEmpty(path)) {
			throw new IllegalArgumentException("Empty path");
		}
		if (variables != null) {
			for (Map.Entry<String, Object> entry : variables.entrySet()) {
				String variable = variableIndicator(entry.getKey());
				path = path.replaceAll(variable, entry.getValue().toString());
			}
		}
		path = path.replaceAll(EMPTY_VARIABLE, EMPTY_STRING);
		return path;
	}

	private static String variableIndicator(String key) {
		return format(VARIABLE_INDICATOR, key);
	}

}
