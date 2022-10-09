/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.error;

import io.imunity.furms.api.validation.exceptions.IdNotFoundValidationError;
import io.imunity.furms.domain.authz.roles.IncorrectResourceIdException;
import io.imunity.furms.rest.error.exceptions.RestNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import io.imunity.furms.domain.users.UnknownUserException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.lang.invoke.MethodHandles;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class RestExceptionHandlers {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final ResponseEntityExceptionHandler defaultExceptionHandler = new ResponseEntityExceptionHandler() {
		@Override
		protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
			return super.handleHttpRequestMethodNotSupported(ex, headers, status, request);
		}
	};

	@ExceptionHandler({UnknownUserException.class})
	ResponseEntity<RestExceptionData> handleUnknownUserException(UnknownUserException ex, HttpServletRequest request) {
		LOG.error("UnknownUserException during REST operation: ", ex);
		return ResponseEntity
				.status(NOT_FOUND)
				.body(RestExceptionData.builder()
						.message(format("User %s not found", ex.userId.id))
						.error(NOT_FOUND.getReasonPhrase())
						.path(request.getRequestURI())
						.build());
	}

	@ExceptionHandler({AccessDeniedException.class})
	ResponseEntity<RestExceptionData> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
		LOG.error("AccessDeniedException during REST operation: ", ex);
		return ResponseEntity
				.status(FORBIDDEN)
				.body(RestExceptionData.builder()
						.message(ex.getMessage())
						.error(FORBIDDEN.getReasonPhrase())
						.path(request.getRequestURI())
						.build());
	}

	@ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
	ResponseEntity<RestExceptionData> handleIllegalArgumentException(RuntimeException ex, HttpServletRequest request) {
		LOG.error(ex.getClass().getName() + " during REST operation: ", ex);
		return ResponseEntity
				.status(BAD_REQUEST)
				.body(RestExceptionData.builder()
						.message(ex.getMessage())
						.error(BAD_REQUEST.getReasonPhrase())
						.path(request.getRequestURI())
						.build());
	}

	@ExceptionHandler({
			RestNotFoundException.class,
			IdNotFoundValidationError.class,
			IncorrectResourceIdException.class})
	ResponseEntity<RestExceptionData> handleRestNotFound(RuntimeException ex, HttpServletRequest request) {
		LOG.error("REST requested element not found: ", ex);
		return ResponseEntity
				.status(NOT_FOUND)
				.body(RestExceptionData.builder()
						.message(ex.getMessage())
						.error(NOT_FOUND.getReasonPhrase())
						.path(request.getRequestURI())
						.build());
	}

	@ExceptionHandler({Exception.class})
	ResponseEntity<RestExceptionData> handlerDefault(Exception ex, WebRequest webRequest, HttpServletRequest request) {
		LOG.error("Unexpected exception during REST operation: ", ex);
		RestExceptionData.RestExceptionDataBuilder data = RestExceptionData.builder()
				.message(ex.getMessage())
				.path(request.getRequestURI());
		try {
			ResponseEntity<Object> defaultResponse = defaultExceptionHandler.handleException(ex, webRequest);
			return ResponseEntity
					.status(defaultResponse.getStatusCode())
					.body(data.error(defaultResponse.getStatusCode().getReasonPhrase()).build());
		} catch (Exception internalException) {
			LOG.error("Internal exception during handle REST exceptions: ", internalException);
			return ResponseEntity
					.status(INTERNAL_SERVER_ERROR)
					.body(data.error(INTERNAL_SERVER_ERROR.getReasonPhrase()).build());
		}

	}
}