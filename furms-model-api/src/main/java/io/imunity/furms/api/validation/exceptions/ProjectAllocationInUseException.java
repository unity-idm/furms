/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ProjectAllocationInUseException extends IllegalArgumentException {

    public ProjectAllocationInUseException(String message) {
        super(message);
    }
}
