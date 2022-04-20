/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;

import java.time.LocalDateTime;
import java.util.Objects;

public class SSHKeyOperationJob {
	public final SSHKeyOperationJobId id;
	public final SiteId siteId;
	public final SSHKeyId sshkeyId;
	public final CorrelationId correlationId;
	public final SSHKeyOperationStatus status;
	public final SSHKeyOperation operation;
	public final String error;
	public final LocalDateTime originationTime;

	SSHKeyOperationJob(SSHKeyOperationJobId id, SiteId siteId, SSHKeyId sshkeyId, CorrelationId correlationId,
			SSHKeyOperation operation, SSHKeyOperationStatus status, String error,
			LocalDateTime originationTime) {
		this.id = id;
		this.siteId = siteId;
		this.sshkeyId = sshkeyId;
		this.correlationId = correlationId;
		this.operation = operation;
		this.status = status;
		this.error = error;
		this.originationTime = originationTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SSHKeyOperationJob that = (SSHKeyOperationJob) o;
		return Objects.equals(id, that.id) && Objects.equals(siteId, that.siteId)
				&& Objects.equals(sshkeyId, that.sshkeyId)
				&& Objects.equals(correlationId, that.correlationId) && operation == that.operation
				&& status == that.status && Objects.equals(error, that.error)
						&& Objects.equals(originationTime, that.originationTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, sshkeyId, correlationId, operation, status, error, originationTime);
	}

	@Override
	public String toString() {
		return "SSHKeyInstallationJob{" + "id='" + id + '\'' + ", siteId='" + siteId + '\'' + ", sshkeyId='"
				+ sshkeyId + '\'' + ", correlationId=" + correlationId + ", status=" + status
				+ ", operation=" + operation + ", error=" + error + ", originationTime=" + originationTime + '}';
	}

	public static SSHKeyOperationJobBuilder builder() {
		return new SSHKeyOperationJobBuilder();
	}

	public static final class SSHKeyOperationJobBuilder {
		public SSHKeyOperationJobId id;
		public SiteId siteId;
		public SSHKeyId sshkeyId;
		public CorrelationId correlationId;
		public SSHKeyOperationStatus status;
		public SSHKeyOperation operation;
		public String error;
		public LocalDateTime originationTime;
		
		private SSHKeyOperationJobBuilder() {
		}

		public SSHKeyOperationJobBuilder id(String id) {
			this.id = new SSHKeyOperationJobId(id);
			return this;
		}

		public SSHKeyOperationJobBuilder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public SSHKeyOperationJobBuilder sshkeyId(String sshkeyId) {
			this.sshkeyId = new SSHKeyId(sshkeyId);
			return this;
		}

		public SSHKeyOperationJobBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public SSHKeyOperationJobBuilder status(SSHKeyOperationStatus status) {
			this.status = status;
			return this;
		}

		public SSHKeyOperationJobBuilder operation(SSHKeyOperation operation) {
			this.operation = operation;
			return this;
		}

		public SSHKeyOperationJobBuilder error(String error) {
			this.error = error;
			return this;
		}
		
		public SSHKeyOperationJobBuilder originationTime(LocalDateTime originationTime) {
			this.originationTime = originationTime;
			return this;
		}

		public SSHKeyOperationJob build() {
			return new SSHKeyOperationJob(id, siteId, sshkeyId, correlationId, operation, status, error, originationTime);
		}
	}
}
