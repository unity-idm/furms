/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ProjectExpiredException extends IllegalArgumentException {

    public ProjectExpiredException(String message) {
        super(message);
    }
}
