/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.List;
import java.util.Objects;

class Site {
	public final String id;
	public final String name;
	public final String sitePolicyId;
	public final List<ResourceCredit> resourceCredits;
	public final List<ResourceType> resourceTypes;
	public final List<InfraService> services;
	public final List<Policy> policies;
	
	Site(String id, String name, String sitePolicyId, List<ResourceCredit> resourceCredits, List<ResourceType> resourceTypes,
	     List<InfraService> services, List<Policy> policies) {
		this.id = id;
		this.name = name;
		this.sitePolicyId = sitePolicyId;
		this.resourceCredits = resourceCredits;
		this.resourceTypes = resourceTypes;
		this.services = services;
		this.policies = policies;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Site site = (Site) o;
		return Objects.equals(id, site.id)
				&& Objects.equals(name, site.name)
				&& Objects.equals(resourceCredits, site.resourceCredits)
				&& Objects.equals(resourceTypes, site.resourceTypes)
				&& Objects.equals(services, site.services)
				&& Objects.equals(policies, site.policies);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, resourceCredits, resourceTypes, services, policies);
	}

	@Override
	public String toString() {
		return "Site{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", resourceCredits=" + resourceCredits +
				", resourceTypes=" + resourceTypes +
				", services=" + services +
				", policies=" + policies +
				'}';
	}
}
