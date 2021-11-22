/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.user_site_access;

import io.imunity.furms.domain.policy_documents.PolicyDocumentEvent;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class UserSiteAccessRevokedEvent implements PolicyDocumentEvent {
	public final FenixUserId fenixUserId;

	public UserSiteAccessRevokedEvent(FenixUserId fenixUserId) {
		this.fenixUserId = fenixUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserSiteAccessRevokedEvent siteEvent = (UserSiteAccessRevokedEvent) o;
			return Objects.equals(fenixUserId, siteEvent.fenixUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId);
	}

	@Override
	public String toString() {
		return "UserSiteAccessGrantedEvent{" +
			"fenixUserId='" + fenixUserId + '\'' +
			'}';
	}
}
