/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.utils;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreamUtils {
	public static <T> Predicate<T> distinctBy(Function<? super T, ?> extractorFunction) {
		Set<Object> occurredElements = ConcurrentHashMap.newKeySet();
		return key -> occurredElements.add(extractorFunction.apply(key));
	}
}
