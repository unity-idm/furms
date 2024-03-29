/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.alarms;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;

import java.util.Objects;

public class FiredAlarm {
	public final AlarmId alarmId;
	public final ProjectId projectId;
	public final ProjectAllocationId projectAllocationId;
	public final String projectAllocationName;
	public final String alarmName;

	public FiredAlarm(AlarmId alarmId, ProjectId projectId, ProjectAllocationId projectAllocationId, String projectAllocationName, String alarmName) {
		this.alarmId = alarmId;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.projectAllocationName = projectAllocationName;
		this.alarmName = alarmName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FiredAlarm that = (FiredAlarm) o;
		return Objects.equals(alarmId, that.alarmId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(projectAllocationName, that.projectAllocationName) &&
			Objects.equals(alarmName, that.alarmName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(alarmId, projectId, projectAllocationId, projectAllocationName, alarmName);
	}

	@Override
	public String toString() {
		return "ActiveAlarm{" +
			"alarmId=" + alarmId +
			", projectId='" + projectId + '\'' +
			", projectAllocationId='" + projectAllocationId + '\'' +
			", projectAllocationName='" + projectAllocationName + '\'' +
			", alarmName='" + alarmName + '\'' +
			'}';
	}

	public static ActiveAlarmBuilder builder() {
		return new ActiveAlarmBuilder();
	}

	public static final class ActiveAlarmBuilder {
		public AlarmId alarmId;
		public ProjectId projectId;
		public ProjectAllocationId projectAllocationId;
		public String projectAllocationName;
		public String alarmName;

		private ActiveAlarmBuilder() {
		}

		public ActiveAlarmBuilder alarmId(AlarmId alarmId) {
			this.alarmId = alarmId;
			return this;
		}

		public ActiveAlarmBuilder projectId(ProjectId projectId) {
			this.projectId = projectId;
			return this;
		}

		public ActiveAlarmBuilder projectAllocationId(ProjectAllocationId projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ActiveAlarmBuilder projectAllocationName(String projectAllocationName) {
			this.projectAllocationName = projectAllocationName;
			return this;
		}

		public ActiveAlarmBuilder alarmName(String alarmName) {
			this.alarmName = alarmName;
			return this;
		}

		public FiredAlarm build() {
			return new FiredAlarm(alarmId, projectId, projectAllocationId, projectAllocationName, alarmName);
		}
	}
}
