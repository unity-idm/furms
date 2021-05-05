/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.time.LocalDateTime;
import java.util.Objects;

public class SSHKeyOperationJob {
	public final String id;
	public final String siteId;
	public final String sshkeyId;
	public final CorrelationId correlationId;
	public final SSHKeyOperationStatus status;
	public final SSHKeyOperation operation;
	public final String error;
	public final LocalDateTime originationTime;

	SSHKeyOperationJob(String id, String siteId, String sshkeyId, CorrelationId correlationId,
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

	public static ProjectInstallationJobBuilder builder() {
		return new ProjectInstallationJobBuilder();
	}

	public static final class ProjectInstallationJobBuilder {
		public String id;
		public String siteId;
		public String sshkeyId;
		public CorrelationId correlationId;
		public SSHKeyOperationStatus status;
		public SSHKeyOperation operation;
		public String error;
		public LocalDateTime originationTime;
		
		private ProjectInstallationJobBuilder() {
		}

		public ProjectInstallationJobBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectInstallationJobBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationJobBuilder sshkeyId(String sshkeyId) {
			this.sshkeyId = sshkeyId;
			return this;
		}

		public ProjectInstallationJobBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectInstallationJobBuilder status(SSHKeyOperationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJobBuilder operation(SSHKeyOperation operation) {
			this.operation = operation;
			return this;
		}

		public ProjectInstallationJobBuilder error(String error) {
			this.error = error;
			return this;
		}
		
		public ProjectInstallationJobBuilder originationTime(LocalDateTime originationTime) {
			this.originationTime = originationTime;
			return this;
		}

		public SSHKeyOperationJob build() {
			return new SSHKeyOperationJob(id, siteId, sshkeyId, correlationId, operation, status, error, originationTime);
		}
	}
}
