/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent_pending_messages;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class SiteAgentPendingMessage {
	public final SiteExternalId siteExternalId;
	public final CorrelationId correlationId;
	public final int retryCount;
	public final String jsonContent;
	public final LocalDateTime utcSentAt;
	public final LocalDateTime utcAckAt;
	public final Optional<ErrorMessage> errorMessage;


	SiteAgentPendingMessage(SiteExternalId siteExternalId, CorrelationId correlationId,
	                        int retryCount, String jsonContent, LocalDateTime utcSentAt, LocalDateTime utcAckAt,
	                        Optional<ErrorMessage> errorMessage) {
		this.siteExternalId = siteExternalId;
		this.correlationId = correlationId;
		this.retryCount = retryCount;
		this.jsonContent = jsonContent;
		this.utcSentAt = utcSentAt;
		this.utcAckAt = utcAckAt;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAgentPendingMessage that = (SiteAgentPendingMessage) o;
		return Objects.equals(siteExternalId, that.siteExternalId) &&
			Objects.equals(correlationId, that.correlationId) &&
			retryCount == that.retryCount &&
			Objects.equals(jsonContent, that.jsonContent) &&
			Objects.equals(utcSentAt, that.utcSentAt) &&
			Objects.equals(utcAckAt, that.utcAckAt) &&
			Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteExternalId, correlationId, retryCount, jsonContent, utcSentAt, utcAckAt, errorMessage);
	}

	@Override
	public String toString() {
		return "SiteAgentPendingMessage{" +
			"siteId='" + siteExternalId + '\'' +
			", correlationId=" + correlationId +
			", retryAmount=" + retryCount +
			", jsonContent='" + jsonContent + '\'' +
			", utcSentAt=" + utcSentAt +
			", utcAckAt=" + utcAckAt +
			", errorMessage=" + errorMessage +
			'}';
	}

	public static SiteAgentPendingMessageBuilder builder() {
		return new SiteAgentPendingMessageBuilder();
	}

	public static final class SiteAgentPendingMessageBuilder {
		public SiteExternalId siteId;
		public CorrelationId correlationId;
		public int retryCount;
		public String jsonContent;
		public LocalDateTime utcSentAt;
		public LocalDateTime utcAckAt;
		public Optional<ErrorMessage> errorStatus = Optional.empty();


		private SiteAgentPendingMessageBuilder() {
		}

		public SiteAgentPendingMessageBuilder siteExternalId(SiteExternalId siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteAgentPendingMessageBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public SiteAgentPendingMessageBuilder jsonContent(String jsonContent) {
			this.jsonContent = jsonContent;
			return this;
		}

		public SiteAgentPendingMessageBuilder utcSentAt(LocalDateTime utcSentAt) {
			this.utcSentAt = utcSentAt;
			return this;
		}

		public SiteAgentPendingMessageBuilder utcAckAt(LocalDateTime utcAckAt) {
			this.utcAckAt = utcAckAt;
			return this;
		}

		public SiteAgentPendingMessageBuilder retryCount(int retryCount) {
			this.retryCount = retryCount;
			return this;
		}

		public SiteAgentPendingMessageBuilder errorMessage(String code, String message) {
			if (code == null && message == null)
				return this;
			this.errorStatus = Optional.of(new ErrorMessage(code, message));
			return this;
		}

		public SiteAgentPendingMessage build() {
			return new SiteAgentPendingMessage(siteId, correlationId, retryCount, jsonContent, utcSentAt, utcAckAt, errorStatus);
		}
	}
}
