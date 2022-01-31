/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.logs;

import io.imunity.furms.domain.audit_log.Action;
import io.imunity.furms.domain.audit_log.Operation;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

class AuditLogGridModel {
	public final String id;
	public final ZonedDateTime timestamp;
	public final String originator;
	public final Operation operation;
	public final Action action;
	public final String name;
	public final Map<String, Object> data;

	AuditLogGridModel(String id, ZonedDateTime timestamp, String originator, Operation operation, Action action, String name, Map<String, Object> data) {
		this.id = id;
		this.timestamp = timestamp;
		this.originator = originator;
		this.operation = operation;
		this.action = action;
		this.name = name;
		this.data = data;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AuditLogGridModel that = (AuditLogGridModel) o;
		return Objects.equals(timestamp, that.timestamp) &&
			Objects.equals(id, that.id) &&
			Objects.equals(originator, that.originator) &&
			operation == that.operation &&
			action == that.action &&
			Objects.equals(name, that.name) &&
			Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, timestamp, originator, operation, action, name, data);
	}

	@Override
	public String toString() {
		return "AuditLogGridModel{" +
			"timestamp=" + timestamp +
			", id='" + id + '\'' +
			", originator='" + originator + '\'' +
			", operation=" + operation +
			", action=" + action +
			", name='" + name + '\'' +
			", data=" + data +
			'}';
	}

	public static AuditLogGridModelBuilder builder() {
		return new AuditLogGridModelBuilder();
	}

	public static final class AuditLogGridModelBuilder {
		public String id;
		public ZonedDateTime timestamp;
		public String originator;
		public Operation operation;
		public Action action;
		public String name;
		public Map<String, Object> data;

		private AuditLogGridModelBuilder() {
		}

		public AuditLogGridModelBuilder id(String id) {
			this.id = id;
			return this;
		}

		public AuditLogGridModelBuilder timestamp(ZonedDateTime timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public AuditLogGridModelBuilder originator(String originator) {
			this.originator = originator;
			return this;
		}

		public AuditLogGridModelBuilder operation(Operation operation) {
			this.operation = operation;
			return this;
		}

		public AuditLogGridModelBuilder action(Action action) {
			this.action = action;
			return this;
		}

		public AuditLogGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AuditLogGridModelBuilder data(Map<String, Object> data) {
			this.data = data;
			return this;
		}

		public AuditLogGridModel build() {
			return new AuditLogGridModel(id, timestamp, originator, operation, action, name, data);
		}
	}
}
