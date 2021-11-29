/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Set;

public class AlarmWithUserIds {
	public final AlarmId id;
	public final String projectId;
	public final String projectAllocationId;
	public final String name;
	public final int threshold;
	public final boolean allUsers;
	public final Set<FenixUserId> alarmUser;

	private AlarmWithUserIds(AlarmId id, String projectId, String projectAllocationId, String name, int threshold, boolean allUsers, Set<FenixUserId> alarmUser) {
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.name = name;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.alarmUser = alarmUser;
	}

	public AlarmWithUserIds(AlarmWithUserEmails alarm, Set<FenixUserId> alarmUser) {
		this.id = new AlarmId(alarm.id);
		this.projectId = alarm.projectId;
		this.projectAllocationId = alarm.projectAllocationId;
		this.name = alarm.name;
		this.threshold = alarm.threshold;
		this.allUsers = alarm.allUsers;
		this.alarmUser = alarmUser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmWithUserIds alarm = (AlarmWithUserIds) o;
		return threshold == alarm.threshold &&
			allUsers == alarm.allUsers &&
			Objects.equals(id, alarm.id) &&
			Objects.equals(projectId, alarm.projectId) &&
			Objects.equals(projectAllocationId, alarm.projectAllocationId) &&
			Objects.equals(name, alarm.name) &&
			Objects.equals(alarmUser, alarm.alarmUser);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, name, threshold, allUsers, alarmUser);
	}

	@Override
	public String toString() {
		return "Alarm{" +
			"id='" + id + '\'' +
			", projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", name='" + name + '\'' +
			", threshold=" + threshold +
			", allUsers=" + allUsers +
			", alarmUser=" + alarmUser +
			'}';
	}

	public static AlarmBuilder builder() {
		return new AlarmBuilder();
	}

	public static final class AlarmBuilder {
		public AlarmId id;
		public String projectId;
		public String projectAllocationId;
		public String name;
		public int threshold;
		public boolean allUsers;
		public Set<FenixUserId> alarmUser;

		private AlarmBuilder() {
		}

		public AlarmBuilder id(AlarmId id) {
			this.id = id;
			return this;
		}

		public AlarmBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public AlarmBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public AlarmBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AlarmBuilder threshold(int threshold) {
			this.threshold = threshold;
			return this;
		}

		public AlarmBuilder allUsers(boolean allUsers) {
			this.allUsers = allUsers;
			return this;
		}

		public AlarmBuilder alarmUser(Set<FenixUserId> alarmUser) {
			this.alarmUser = alarmUser;
			return this;
		}

		public AlarmWithUserIds build() {
			return new AlarmWithUserIds(id, projectId, projectAllocationId, name, threshold, allUsers, alarmUser);
		}
	}
}
