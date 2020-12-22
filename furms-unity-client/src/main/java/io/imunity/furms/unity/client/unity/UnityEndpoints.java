/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "furms.unity.endpoints.unity")
public class UnityEndpoints {

	private final String group;

	public UnityEndpoints(String group) {
		this.group = group;
	}

	public String getGroup() {
		return group;
	}
}
