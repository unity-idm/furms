/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project_allocation;

import io.imunity.furms.domain.alarms.AlarmWithUserEmails;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class ProjectAllocationDataSnapshot {
	private final Map<ProjectAllocationId, ProjectAllocationInstallation> allocationByProjectAllocation;
	private final Map<ProjectAllocationId, ProjectDeallocation> deallocationsByProjectAllocationId;
	private final Map<ProjectAllocationId, Set<ProjectAllocationChunk>> chunksByProjectAllocationId;
	private final Map<ProjectAllocationId, Integer> alarmsThresholdByProjectAllocationId;

	public ProjectAllocationDataSnapshot(Set<ProjectAllocationInstallation> installations, Set<ProjectDeallocation> uninstallations,
	                                     Set<ProjectAllocationChunk> chunks, Set<AlarmWithUserEmails> alarms) {
		this.allocationByProjectAllocation = installations.stream()
			.collect(toMap(installation -> installation.projectAllocationId, identity()));
		this.deallocationsByProjectAllocationId = uninstallations.stream()
			.collect(toMap(uninstallation -> uninstallation.projectAllocationId, identity()));
		this.chunksByProjectAllocationId = chunks.stream()
			.collect(groupingBy(chunk -> chunk.projectAllocationId, toSet()));
		this.alarmsThresholdByProjectAllocationId = alarms.stream()
			.collect(toMap(alarm -> alarm.projectAllocationId, alarm -> alarm.threshold));
	}

	public ProjectAllocationDataSnapshot(Set<ProjectAllocationInstallation> installations, Set<ProjectDeallocation> uninstallations,
	                                     Set<ProjectAllocationChunk> chunks) {
		this(installations, uninstallations, chunks, Set.of());
	}

	public Optional<ProjectAllocationInstallation> getAllocation(ProjectAllocationId projectAllocationId) {
		return Optional.ofNullable(allocationByProjectAllocation.get(projectAllocationId));
	}

	public Set<ProjectAllocationChunk> getChunks(ProjectAllocationId projectAllocationId) {
		return chunksByProjectAllocationId.getOrDefault(projectAllocationId, Set.of());
	}

	public Optional<ProjectDeallocation> getDeallocationStatus(ProjectAllocationId projectAllocationId) {
		return Optional.ofNullable(deallocationsByProjectAllocationId.get(projectAllocationId));
	}

	public int getAlarmThreshold(ProjectAllocationId projectAllocationId) {
		return alarmsThresholdByProjectAllocationId.getOrDefault(projectAllocationId, 0);
	}
}
