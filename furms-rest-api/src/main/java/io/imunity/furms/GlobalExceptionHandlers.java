/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.imunity.furms.domain.users.UnknownUserException;

@ControllerAdvice
class GlobalExceptionHandlers extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(value = { UnknownUserException.class })
	ResponseEntity<Object> handleConflict(UnknownUserException ex, WebRequest request) {
		return handleExceptionInternal(ex, "User " + ex.userId + " not found", 
				new HttpHeaders(), HttpStatus.NOT_FOUND, request);
	}
}
