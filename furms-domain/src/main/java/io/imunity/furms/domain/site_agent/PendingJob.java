/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PendingJob<J> {
	public final CompletableFuture<J> jobFuture;
	public final String correlationId;

	public PendingJob(CompletableFuture<J> jobFuture, String correlationId) {
		this.jobFuture = jobFuture;
		this.correlationId = correlationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PendingJob<?> that = (PendingJob<?>) o;
		return Objects.equals(jobFuture, that.jobFuture) &&
			Objects.equals(correlationId, that.correlationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(jobFuture, correlationId);
	}

	@Override
	public String toString() {
		return "PendingJob{" +
			"jobFuture=" + jobFuture +
			", correlationId='" + correlationId + '\'' +
			'}';
	}
}
