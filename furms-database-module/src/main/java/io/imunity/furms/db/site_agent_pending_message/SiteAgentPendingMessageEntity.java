/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Table("site_agent_pending_message")
class SiteAgentPendingMessageEntity extends UUIDIdentifiable {
	public final UUID siteId;
	public final String siteExternalId;
	public final UUID correlationId;
	public final int retryCount;
	public final String jsonContent;
	public final LocalDateTime sentAt;
	public final LocalDateTime ackAt;

	SiteAgentPendingMessageEntity(UUID id, UUID siteId, String siteExternalId, UUID correlationId,
	                              int retryCount, String jsonContent, LocalDateTime sentAt, LocalDateTime ackAt) {
		this.id = id;
		this.siteId = siteId;
		this.siteExternalId = siteExternalId;
		this.correlationId = correlationId;
		this.retryCount = retryCount;
		this.jsonContent = jsonContent;
		this.sentAt = sentAt;
		this.ackAt = ackAt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAgentPendingMessageEntity that = (SiteAgentPendingMessageEntity) o;
		return retryCount == that.retryCount &&
			Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteExternalId, that.siteExternalId) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(jsonContent, that.jsonContent) &&
			Objects.equals(sentAt, that.sentAt) &&
			Objects.equals(ackAt, that.ackAt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, siteExternalId, correlationId, retryCount, jsonContent, sentAt, ackAt);
	}

	public static SiteAgentPendingMessageEntityBuilder builder() {
		return new SiteAgentPendingMessageEntityBuilder();
	}


	public static final class SiteAgentPendingMessageEntityBuilder {
		private UUID id;
		private UUID siteId;
		private String siteExternalId;
		private UUID correlationId;
		private int retryCount;
		private String jsonContent;
		private LocalDateTime sentAt;
		private LocalDateTime ackAt;

		private SiteAgentPendingMessageEntityBuilder() {
		}

		public SiteAgentPendingMessageEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder siteExternalId(String siteExternalId) {
			this.siteExternalId = siteExternalId;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder retryCount(int retryCount) {
			this.retryCount = retryCount;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder jsonContent(String jsonContent) {
			this.jsonContent = jsonContent;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder sentAt(LocalDateTime sentAt) {
			this.sentAt = sentAt;
			return this;
		}

		public SiteAgentPendingMessageEntityBuilder ackAt(LocalDateTime ackAt) {
			this.ackAt = ackAt;
			return this;
		}

		public SiteAgentPendingMessageEntity build() {
			return new SiteAgentPendingMessageEntity(id, siteId, siteExternalId, correlationId, retryCount, jsonContent, sentAt, ackAt);
		}
	}
}
