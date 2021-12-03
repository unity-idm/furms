/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.alarms;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Table("alarm_configuration")
class AlarmEntity extends UUIDIdentifiable {
	public final UUID projectId;
	public final UUID projectAllocationId;
	public final String name;
	public final int threshold;
	public final boolean allUsers;
	public final boolean fired;
	@MappedCollection(idColumn = "alarm_id")
	public final Set<AlarmUserEntity> alarmUserEntities;

	AlarmEntity(UUID id, UUID projectId, UUID projectAllocationId, String name, int threshold,
	            boolean allUsers, boolean fired, Set<AlarmUserEntity> alarmUserEntities) {
		this.id = id;
		this.projectId = projectId;
		this.projectAllocationId = projectAllocationId;
		this.name = name;
		this.threshold = threshold;
		this.allUsers = allUsers;
		this.fired = fired;
		this.alarmUserEntities = alarmUserEntities;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AlarmEntity that = (AlarmEntity) o;
		return allUsers == that.allUsers &&
			fired == that.fired &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(name, that.name) &&
			Objects.equals(threshold, that.threshold) &&
			Objects.equals(alarmUserEntities, that.alarmUserEntities);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectAllocationId, name, threshold, allUsers, fired, alarmUserEntities);
	}

	@Override
	public String toString() {
		return "AlarmEntity{" +
			"projectId=" + projectId +
			", projectAllocationId=" + projectAllocationId +
			", name='" + name + '\'' +
			", threshold='" + threshold + '\'' +
			", fired=" + fired +
			", allUsers=" + allUsers +
			", alarmUserEntities=" + alarmUserEntities +
			", id=" + id +
			'}';
	}

	public static AlarmEntityBuilder builder() {
		return new AlarmEntityBuilder();
	}

	public static final class AlarmEntityBuilder {
		private UUID id;
		private UUID projectId;
		private UUID projectAllocationId;
		private String name;
		private int threshold;
		private boolean allUsers;
		private boolean fired;
		private Set<AlarmUserEntity> alarmUserEntities;

		private AlarmEntityBuilder() {
		}

		public AlarmEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public AlarmEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public AlarmEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AlarmEntityBuilder threshold(int threshold) {
			this.threshold = threshold;
			return this;
		}

		public AlarmEntityBuilder allUsers(boolean allUsers) {
			this.allUsers = allUsers;
			return this;
		}

		public AlarmEntityBuilder fired(boolean fired) {
			this.fired = fired;
			return this;
		}

		public AlarmEntityBuilder alarmUserEntities(Set<AlarmUserEntity> alarmUserEntities) {
			this.alarmUserEntities = alarmUserEntities;
			return this;
		}

		public AlarmEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public AlarmEntity build() {
			return new AlarmEntity(id, projectId, projectAllocationId, name, threshold, allUsers, fired, alarmUserEntities);
		}
	}
}
