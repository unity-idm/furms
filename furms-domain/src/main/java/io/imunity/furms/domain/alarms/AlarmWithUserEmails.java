/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import java.util.Objects;
import java.util.Set;

public class AlarmWithUserEmails {
	public final AlarmId id;
	public final String projectId;
	public final String projectAllocationId;
	public final String name;
	public final int threshold;
	public final boolean allUsers;
	public final boolean fired;
	public final Set<String> alarmUserEmails;

	private AlarmWithUserEmails(AlarmId id, String projectId, String projectAllocationId, String name, int threshold, boolean allUsers, boolean fired, Set<String> alarmUserEmails) {
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.name = name;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.fired = fired;
		this.alarmUserEmails = alarmUserEmails;
	}

	public AlarmWithUserEmails(AlarmWithUserIds alarm, Set<String> alarmUserEmails) {
		this.id = new AlarmId(alarm.id);
		this.projectId = alarm.projectId;
		this.projectAllocationId = alarm.projectAllocationId;
		this.name = alarm.name;
		this.threshold = alarm.threshold;
		this.allUsers = alarm.allUsers;
		this.fired = alarm.fired;
		this.alarmUserEmails = alarmUserEmails;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmWithUserEmails alarm = (AlarmWithUserEmails) o;
		return threshold == alarm.threshold &&
			allUsers == alarm.allUsers &&
			fired == alarm.fired &&
			Objects.equals(id, alarm.id) &&
			Objects.equals(projectId, alarm.projectId) &&
			Objects.equals(projectAllocationId, alarm.projectAllocationId) &&
			Objects.equals(name, alarm.name) &&
			Objects.equals(alarmUserEmails, alarm.alarmUserEmails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, name, threshold, allUsers, fired, alarmUserEmails);
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
			", fired=" + fired +
			", alarmUser=" + alarmUserEmails +
			'}';
	}

	public static AlarmWithUserEmailsBuilder builder() {
		return new AlarmWithUserEmailsBuilder();
	}

	public static final class AlarmWithUserEmailsBuilder {
		public AlarmId id;
		public String projectId;
		public String projectAllocationId;
		public String name;
		public int threshold;
		public boolean allUsers;
		public boolean fired;
		public Set<String> alarmUserEmails;

		private AlarmWithUserEmailsBuilder() {
		}

		public AlarmWithUserEmailsBuilder id(AlarmId id) {
			this.id = id;
			return this;
		}

		public AlarmWithUserEmailsBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public AlarmWithUserEmailsBuilder projectAllocationId(String projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public AlarmWithUserEmailsBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AlarmWithUserEmailsBuilder threshold(int threshold) {
			this.threshold = threshold;
			return this;
		}

		public AlarmWithUserEmailsBuilder allUsers(boolean allUsers) {
			this.allUsers = allUsers;
			return this;
		}

		public AlarmWithUserEmailsBuilder fired(boolean fired) {
			this.fired = fired;
			return this;
		}

		public AlarmWithUserEmailsBuilder alarmUser(Set<String> alarmUserEmails) {
			this.alarmUserEmails = Set.copyOf(alarmUserEmails);
			return this;
		}

		public AlarmWithUserEmails build() {
			return new AlarmWithUserEmails(id, projectId, projectAllocationId, name, threshold, allUsers, fired, alarmUserEmails);
		}
	}
}
