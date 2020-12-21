/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.capability.Capability;
import io.imunity.furms.core.config.security.user.resource.ResourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FurmsAuthorize {
	Capability capability();
	ResourceType resourceType();
	String id() default "";
}
