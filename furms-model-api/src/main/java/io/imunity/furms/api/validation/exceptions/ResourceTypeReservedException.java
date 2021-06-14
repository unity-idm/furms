/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ResourceTypeReservedException extends IllegalArgumentException {

    public ResourceTypeReservedException(String message) {
        super(message);
    }
}
