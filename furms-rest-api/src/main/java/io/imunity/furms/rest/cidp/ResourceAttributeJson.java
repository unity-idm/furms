/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import java.util.Objects;
import java.util.Set;

public class ResourceAttributeJson {
	public final ResourceIdJson resource;
	public final Set<AttributeJson> attributes;

	public ResourceAttributeJson(ResourceIdJson resource, Set<AttributeJson> attributes) {
		this.resource = resource;
		this.attributes = attributes;
	}

//	public ResourceAttributeJson(Map.Entry<ResourceId, Set<UserAttribute>> entry) {
//		this(new ResourceIdJson(entry.getKey()),
//				entry.getValue().stream().map(AttributeJson::new).collect(toSet()));
//	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAttributeJson that = (ResourceAttributeJson) o;
		return Objects.equals(resource, that.resource) && Objects.equals(attributes, that.attributes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resource, attributes);
	}

	@Override
	public String toString() {
		return "ResourceAttributeJson{" +
				"resource=" + resource +
				", attributes=" + attributes +
				'}';
	}
}
