/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.layout.panels")
public class FurmsLayoutExtraPanelsConfig {

	private final String top;
	private final String left;
	private final String right;
	private final String bottom;

	public FurmsLayoutExtraPanelsConfig(String top, String left, String right, String bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}

	public String getTop() {
		return top;
	}

	public String getLeft() {
		return left;
	}

	public String getRight() {
		return right;
	}

	public String getBottom() {
		return bottom;
	}
}
