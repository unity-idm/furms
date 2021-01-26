/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import java.util.Optional;

public class OptionalException<T> {
	private final T value;
	private final Throwable throwable;

	private OptionalException(T value, Throwable throwable) {
		this.value = value;
		this.throwable = throwable;
	}

	public Optional<T> getValue() {
		return Optional.ofNullable(value);
	}

	public Optional<Throwable> getThrowable(){
			return Optional.ofNullable(throwable);
	}

	public static<T> OptionalException<T> of(T vale){
		return new OptionalException<>(vale, null);
	}

	public static<T> OptionalException<T> of(Throwable throwable){
		return new OptionalException<>(null, throwable);
	}
}
