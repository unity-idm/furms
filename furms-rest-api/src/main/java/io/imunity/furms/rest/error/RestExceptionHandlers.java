/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.imunity.furms.domain.users.UnknownUserException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.lang.invoke.MethodHandles;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
class RestExceptionHandlers {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({UnknownUserException.class})
	RestExceptionData handleUnknownUserException(UnknownUserException ex) {
		LOG.error("UnknownUserException during REST operation: ", ex);
		return RestExceptionData.builder()
				.message(format("User %s not found", ex.userId))
				.error(ex.getClass().getSimpleName())
				.build();
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ExceptionHandler({NoHandlerFoundException.class})
	RestExceptionData handleNoHandlerFoundException(NoHandlerFoundException ex) {
		return RestExceptionData.builder()
				.error(NOT_FOUND.getReasonPhrase())
				.path(ex.getRequestURL())
				.build();
	}

	@ResponseStatus(INTERNAL_SERVER_ERROR)
	@ExceptionHandler({Exception.class})
	RestExceptionData handlerDefault(Throwable ex) {
		LOG.error("Unexpected exception during REST operation: ", ex);
		return RestExceptionData.builder()
				.error(INTERNAL_SERVER_ERROR.getReasonPhrase())
				.build();
	}
}