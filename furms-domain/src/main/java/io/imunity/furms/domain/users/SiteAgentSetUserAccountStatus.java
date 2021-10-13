/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_operation.UserAddition;

import java.util.Objects;

public class SiteAgentSetUserAccountStatus {
	public final SiteExternalId siteExternalId;
	public final CorrelationId correlationId;
	public final FenixUserId fenixUserId;
	public final UserStatus status;
	public final UserAccountStatusUpdateReason reason;

	public SiteAgentSetUserAccountStatus(SiteExternalId siteExternalId,
	                                     CorrelationId correlationId,
	                                     FenixUserId fenixUserId,
	                                     UserStatus status,
	                                     UserAccountStatusUpdateReason reason) {
		this.siteExternalId = siteExternalId;
		this.correlationId = correlationId;
		this.fenixUserId = fenixUserId;
		this.status = status;
		this.reason = reason;
	}

	public SiteAgentSetUserAccountStatus(UserAddition userAddition, UserStatus status, UserAccountStatusUpdateReason reason) {
		this(userAddition.siteId.externalId, userAddition.correlationId, new FenixUserId(userAddition.userId),
				status, reason);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAgentSetUserAccountStatus that = (SiteAgentSetUserAccountStatus) o;
		return Objects.equals(siteExternalId, that.siteExternalId)
				&& Objects.equals(correlationId, that.correlationId)
				&& Objects.equals(fenixUserId, that.fenixUserId)
				&& status == that.status
				&& reason == that.reason;
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteExternalId, correlationId, fenixUserId, status, reason);
	}

	@Override
	public String toString() {
		return "SiteAgentSetUserAccountStatus{" +
				"siteExternalId='" + siteExternalId + '\'' +
				", correlationId='" + correlationId + '\'' +
				", fenixUserId=" + fenixUserId +
				", status=" + status +
				", reason='" + reason + '\'' +
				'}';
	}
}
