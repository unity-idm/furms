/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.imunity.furms.domain.users.UnknownUserException;

import java.lang.invoke.MethodHandles;

import static java.lang.String.format;

@RestControllerAdvice
class GlobalExceptionHandlers {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({UnknownUserException.class})
	GlobalExceptionData handleUnknownUserException(UnknownUserException ex) {
		LOG.error("UnknownUserException during REST operation: ", ex);
		return new GlobalExceptionData(format("User %s not found", ex.userId), ex.getClass().getSimpleName());
	}

	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	GlobalExceptionData handlerDefault(Throwable ex) {
		LOG.error("Unexpected exception during REST operation: ", ex);
		return new GlobalExceptionData(ex.getMessage(), ex.getClass().getSimpleName());
	}
}