/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.connection;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.time.LocalDateTime;
import java.util.Objects;

class PendingMessageGridModel {
	public final CorrelationId id;
	public final String operationType;
	public final String status;
	public final String json;
	public final LocalDateTime sentAt;
	public final LocalDateTime ackAt;
	public final int retryAmount;

	PendingMessageGridModel(CorrelationId id, String operationType, String status, String json, LocalDateTime sentAt, LocalDateTime ackAt, int retryAmount) {
		this.id = id;
		this.operationType = operationType;
		this.status = status;
		this.sentAt = sentAt;
		this.json = json;
		this.ackAt = ackAt;
		this.retryAmount = retryAmount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PendingMessageGridModel that = (PendingMessageGridModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "PendingMessageGridModel{" +
			"id=" + id +
			", operationType='" + operationType + '\'' +
			", status='" + status + '\'' +
			", sentAt=" + sentAt +
			", ackAt=" + ackAt +
			", json=" + json +
			", retryAmount=" + retryAmount +
			'}';
	}

	public static PendingMessageGridModelBuilder builder() {
		return new PendingMessageGridModelBuilder();
	}

	public static final class PendingMessageGridModelBuilder {
		public CorrelationId id;
		public String operationType;
		public String status;
		public String json;
		public LocalDateTime sentAt;
		public LocalDateTime ackAt;
		public int retryAmount;

		private PendingMessageGridModelBuilder() {
		}

		public PendingMessageGridModelBuilder id(CorrelationId id) {
			this.id = id;
			return this;
		}

		public PendingMessageGridModelBuilder json(String json) {
			this.json = json;
			return this;
		}

		public PendingMessageGridModelBuilder operationType(String operationType) {
			this.operationType = operationType;
			return this;
		}

		public PendingMessageGridModelBuilder status(String status) {
			this.status = status;
			return this;
		}

		public PendingMessageGridModelBuilder sentAt(LocalDateTime sentAt) {
			this.sentAt = sentAt;
			return this;
		}

		public PendingMessageGridModelBuilder ackAt(LocalDateTime ackAt) {
			this.ackAt = ackAt;
			return this;
		}

		public PendingMessageGridModelBuilder retryAmount(int retryAmount) {
			this.retryAmount = retryAmount;
			return this;
		}

		public PendingMessageGridModel build() {
			return new PendingMessageGridModel(id, operationType, status, json, sentAt, ackAt, retryAmount);
		}
	}
}
