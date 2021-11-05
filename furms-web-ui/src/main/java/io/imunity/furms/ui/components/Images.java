/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

public enum Images {
	FENIX_LOGO("/frontend/common/img/logo-fenix-simple.png");

	public final String path;
	public final String resourcePath;

	private final static String baseResource = "/META-INF/resources";

	private Images(String path) {
		this.path = path;
		this.resourcePath = baseResource + path;
	}
}
