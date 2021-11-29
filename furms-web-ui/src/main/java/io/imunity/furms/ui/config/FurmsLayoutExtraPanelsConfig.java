/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.front.layout.panels")
public class FurmsLayoutExtraPanelsConfig {

	private final Resource top;
	private final Resource left;
	private final Resource right;
	private final Resource bottom;

	public FurmsLayoutExtraPanelsConfig(Resource top, Resource left, Resource right, Resource bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}

	public Resource getTop() {
		return top;
	}

	public Resource getLeft() {
		return left;
	}

	public Resource getRight() {
		return right;
	}

	public Resource getBottom() {
		return bottom;
	}
}
