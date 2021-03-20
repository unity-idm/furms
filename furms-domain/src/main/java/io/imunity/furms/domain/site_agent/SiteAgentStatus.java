/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.site_agent;

import java.time.LocalDateTime;
import java.util.Objects;

public class SiteAgentStatus {
	public final int version;
	public final LocalDateTime checkedTime;
	public final AvailabilityStatus status;

	private SiteAgentStatus(int version, LocalDateTime checkedTime, AvailabilityStatus status) {
		this.version = version;
		this.checkedTime = checkedTime;
		this.status = status;
	}

	public SiteAgentStatus(AvailabilityStatus status) {
		this.version = 1;
		this.checkedTime = LocalDateTime.now();
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAgentStatus that = (SiteAgentStatus) o;
		return Objects.equals(version, that.version) &&
			Objects.equals(checkedTime, that.checkedTime) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, checkedTime, status);
	}

	@Override
	public String toString() {
		return "SiteAgentStatus{" +
			"version='" + version + '\'' +
			", checkedTime=" + checkedTime +
			", status=" + status +
			'}';
	}

	public static SiteAgentStatusBuilder builder() {
		return new SiteAgentStatusBuilder();
	}

	public static final class SiteAgentStatusBuilder {
		public int version;
		public LocalDateTime checkedTime;
		public AvailabilityStatus status;

		private SiteAgentStatusBuilder() {
		}

		public SiteAgentStatusBuilder version(int version) {
			this.version = version;
			return this;
		}

		public SiteAgentStatusBuilder checkedTime(LocalDateTime checkedTime) {
			this.checkedTime = checkedTime;
			return this;
		}

		public SiteAgentStatusBuilder status(AvailabilityStatus status) {
			this.status = status;
			return this;
		}

		public SiteAgentStatus build() {
			return new SiteAgentStatus(version, checkedTime, status);
		}
	}
}
