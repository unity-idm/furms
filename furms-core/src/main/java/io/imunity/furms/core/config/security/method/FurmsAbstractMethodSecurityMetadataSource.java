/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.core.config.security.user.capability.Capability;
import io.imunity.furms.core.config.security.user.resource.ResourceType;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.AbstractMethodSecurityMetadataSource;
import org.springframework.security.access.prepost.PrePostInvocationAttributeFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class FurmsAbstractMethodSecurityMetadataSource extends AbstractMethodSecurityMetadataSource {
	private final PrePostInvocationAttributeFactory attributeFactory;

	public FurmsAbstractMethodSecurityMetadataSource(PrePostInvocationAttributeFactory attributeFactory) {
		this.attributeFactory = attributeFactory;
	}

	@Override
	public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
		Method properMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		FurmsAuthorize annotation = properMethod.getAnnotation(FurmsAuthorize.class);
		if(annotation == null)
			return emptyList();

		Capability capability = annotation.capability();
		ResourceType resourceType = annotation.resourceType();
		String idSpEl = "";
		if(!annotation.id().isEmpty())
			idSpEl = ",#" + annotation.id();
		String furmsSpEl = "hasCapability('" + capability.name() + "','" + resourceType.name() +"'" + idSpEl + ")";

		return singletonList(attributeFactory.createPreInvocationAttribute(null, null, furmsSpEl));
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		return null;
	}
}
