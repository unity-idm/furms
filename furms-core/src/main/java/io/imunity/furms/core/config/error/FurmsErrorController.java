/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.error;

import io.imunity.furms.utils.GlobalExceptionData;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

import static javax.servlet.RequestDispatcher.ERROR_STATUS_CODE;
import static javax.servlet.RequestDispatcher.FORWARD_REQUEST_URI;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Controller
@RequestMapping("/error")
public class FurmsErrorController implements ErrorController {

	@RequestMapping
	public ResponseEntity<GlobalExceptionData> error(HttpServletRequest request) {
		HttpStatus status = getStatus(request);
		return new ResponseEntity<>(GlobalExceptionData.builder()
				.error(status.getReasonPhrase())
				.path(request.getAttribute(FORWARD_REQUEST_URI).toString())
				.build(), status);
	}

	@Override
	public String getErrorPath() {
		return "";
	}

	protected HttpStatus getStatus(HttpServletRequest request) {
		try {
			if (request == null) {
				return INTERNAL_SERVER_ERROR;
			}
			Integer statusCode = (Integer) request.getAttribute(ERROR_STATUS_CODE);
			if (statusCode == null) {
				return INTERNAL_SERVER_ERROR;
			}
			return HttpStatus.valueOf(statusCode);
		}
		catch (Exception ex) {
			return INTERNAL_SERVER_ERROR;
		}
	}


}
