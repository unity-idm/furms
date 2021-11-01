/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.logo.providers;

import io.imunity.furms.ui.user_context.ViewMode;

import static java.lang.String.format;

public class UnsupportedViewModeException extends RuntimeException {

	public UnsupportedViewModeException(ViewMode viewMode) {
		super(format("View mode %s is not supported for getting logo.", viewMode.name()));
	}
}
