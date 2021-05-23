/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import java.util.Optional;

public class OptionalException<T> {
	private final T value;
	private final FrontException exception;

	private OptionalException(T value, FrontException exception) {
		this.value = value;
		this.exception = exception;
	}

	public Optional<T> getValue() {
		return Optional.ofNullable(value);
	}

	public Optional<FrontException> getException(){
			return Optional.ofNullable(exception);
	}

	public static<T> OptionalException<T> of(T vale){
		return new OptionalException<>(vale, null);
	}

	public static<T> OptionalException<T> of(FrontException throwable){
		return new OptionalException<>(null, throwable);
	}
}
