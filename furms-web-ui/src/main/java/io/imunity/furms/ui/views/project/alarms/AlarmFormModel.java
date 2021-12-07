/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.alarms;

import io.imunity.furms.domain.alarms.AlarmId;

import java.util.Objects;
import java.util.Set;

class AlarmFormModel {
	public final AlarmId id;
	public String name;
	public String allocationId;
	public int threshold;
	public boolean allUsers;
	public Set<String> users;

	AlarmFormModel(AlarmId id, String name, String allocationId, int threshold, boolean allUsers, Set<String> users) {
		this.id = id;
		this.name = name;
		this.allocationId = allocationId;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.users = users;
	}

	AlarmFormModel() {
		this.id = null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmFormModel that = (AlarmFormModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "AlarmGridModel{" +
			"alarmId=" + id +
			", name='" + name + '\'' +
			", allocationId='" + allocationId + '\'' +
			", threshold=" + threshold +
			", allUsers=" + allUsers +
			", users=" + users +
			'}';
	}

	public static AlarmGridModelBuilder builder() {
		return new AlarmGridModelBuilder();
	}

	public static final class AlarmGridModelBuilder {
		public AlarmId alarmId;
		public String name;
		public String allocationId;
		public int threshold;
		public boolean allUsers;
		public Set<String> users;

		private AlarmGridModelBuilder() {
		}

		public AlarmGridModelBuilder alarmId(AlarmId alarmId) {
			this.alarmId = alarmId;
			return this;
		}

		public AlarmGridModelBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AlarmGridModelBuilder allocationId(String allocationId) {
			this.allocationId = allocationId;
			return this;
		}

		public AlarmGridModelBuilder threshold(int threshold) {
			this.threshold = threshold;
			return this;
		}

		public AlarmGridModelBuilder allUsers(boolean allUsers) {
			this.allUsers = allUsers;
			return this;
		}

		public AlarmGridModelBuilder users(Set<String> users) {
			this.users = Set.copyOf(users);
			return this;
		}

		public AlarmFormModel build() {
			return new AlarmFormModel(alarmId, name, allocationId, threshold, allUsers, users);
		}
	}
}
