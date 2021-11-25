/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;
import java.util.Set;

public class Alarm {
	public final AlarmId id;
	public final String projectId;
	public final String projectAllocationId;
	public final String name;
	public final int threshold;
	public final boolean allUsers;
	public final Set<FURMSUser> alarmUser;

	private Alarm(AlarmId id, String projectId, String projectAllocationId, String name, int threshold, boolean allUsers, Set<FURMSUser> alarmUser) {
		if(allUsers && !alarmUser.isEmpty())
			throw new IllegalArgumentException("If all user flag is set, the user list should be empty!");
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.name = name;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.alarmUser = alarmUser;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Alarm alarm = (Alarm) o;
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
		public Set<FURMSUser> alarmUser;

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

		public AlarmBuilder alarmUser(Set<FURMSUser> alarmUser) {
			this.alarmUser = alarmUser;
			return this;
		}

		public Alarm build() {
			return new Alarm(id, projectId, projectAllocationId, name, threshold, allUsers, alarmUser);
		}
	}
}
