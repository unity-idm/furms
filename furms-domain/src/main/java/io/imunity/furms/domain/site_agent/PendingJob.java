/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

import io.imunity.furms.domain.sites.SiteExternalId;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PendingJob<J> {
	public final CompletableFuture<J> jobFuture;
	public final CorrelationId correlationId;
	public final SiteExternalId siteExternalId;

	public PendingJob(CompletableFuture<J> jobFuture, CorrelationId correlationId, SiteExternalId siteExternalId) {
		this.jobFuture = jobFuture;
		this.correlationId = correlationId;
		this.siteExternalId = siteExternalId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PendingJob<?> that = (PendingJob<?>) o;
		return Objects.equals(jobFuture, that.jobFuture) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteExternalId, that.siteExternalId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobFuture, correlationId, siteExternalId);
	}

	@Override
	public String toString() {
		return "PendingJob{" +
			"jobFuture=" + jobFuture +
			"siteExternalId=" + siteExternalId +
			", correlationId='" + correlationId + '\'' +
			'}';
	}
}
