/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.alarms;

import io.imunity.furms.domain.alarms.AlarmId;

import java.util.Objects;
import java.util.Set;

class AlarmGridModel {
	public final AlarmId id;
	public final String name;
	public final String allocationName;
	public final int threshold;
	public final boolean allUsers;
	public final Set<String> users;

	AlarmGridModel(AlarmId id, String name, String allocationName, int threshold, boolean allUsers, Set<String> users) {
		this.id = id;
		this.name = name;
		this.allocationName = allocationName;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.users = users;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmGridModel that = (AlarmGridModel) o;
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
			", allocationName='" + allocationName + '\'' +
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
		public String allocationName;
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

		public AlarmGridModelBuilder allocationName(String allocationName) {
			this.allocationName = allocationName;
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

		public AlarmGridModel build() {
			return new AlarmGridModel(alarmId, name, allocationName, threshold, allUsers, users);
		}
	}
}
