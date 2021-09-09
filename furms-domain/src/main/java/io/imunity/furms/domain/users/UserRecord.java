/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.sites.SiteUser;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UserRecord {
	public final UserStatus userStatus;
	public final Set<UserAttribute> attributes;
	public final Map<ResourceId, Set<UserAttribute>> resourceAttributes;
	public final Set<SiteUser> siteInstallations;

	public UserRecord(UserStatus userStatus,
	                  Collection<UserAttribute> attributes,
	                  Map<ResourceId, Set<UserAttribute>> resourceAttributes,
	                  Collection<SiteUser> siteInstallations) {
		this.userStatus = userStatus;
		this.attributes = Set.copyOf(attributes);
		this.resourceAttributes = resourceAttributes;
		this.siteInstallations = Set.copyOf(siteInstallations);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRecord that = (UserRecord) o;
		return userStatus == that.userStatus
				&& Objects.equals(attributes, that.attributes)
				&& Objects.equals(resourceAttributes, that.resourceAttributes)
				&& Objects.equals(siteInstallations, that.siteInstallations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userStatus, attributes, resourceAttributes, siteInstallations);
	}

	@Override
	public String toString() {
		return "UserRecord{" +
				"userStatus=" + userStatus +
				", attributes=" + attributes +
				", resourceAttributes=" + resourceAttributes +
				", siteInstallations=" + siteInstallations +
				'}';
	}
}
