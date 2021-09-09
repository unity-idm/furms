/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

public class UserRecordJson {
	public final UserStatus userStatus;
	public final List<AttributeJson> attributes;
	public final List<ResourceAttributeJson> resourceAttributes;
	public final List<SiteUserJson> siteInstallations;

	UserRecordJson(UserStatus userStatus,
	               Collection<AttributeJson> attributes,
	               List<ResourceAttributeJson> resourceAttributes,
	               Collection<SiteUserJson> siteInstallations) {
		this.userStatus = userStatus;
		this.attributes = List.copyOf(attributes);
		this.resourceAttributes = resourceAttributes;
		this.siteInstallations = List.copyOf(siteInstallations);
	}
	
	UserRecordJson(UserRecord record) {
		this(record.userStatus,
				record.attributes.stream().map(AttributeJson::new).collect(toList()),
				record.resourceAttributes.entrySet().stream().map(ResourceAttributeJson::new).collect(toList()),
				record.siteInstallations.stream().map(SiteUserJson::new).collect(toList()));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserRecordJson that = (UserRecordJson) o;
		return userStatus == that.userStatus
				&& Objects.equals(attributes, that.attributes)
				&& Objects.equals(siteInstallations, that.siteInstallations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userStatus, attributes, siteInstallations);
	}

	@Override
	public String toString() {
		return "UserRecordJson{" +
				"userStatus=" + userStatus +
				", attributes=" + attributes +
				", siteInstallations=" + siteInstallations +
				'}';
	}
}
