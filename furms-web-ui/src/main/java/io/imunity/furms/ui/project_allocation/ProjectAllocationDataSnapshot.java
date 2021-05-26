/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project_allocation;

import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class ProjectAllocationDataSnapshot {
	private final Map<String, ProjectAllocationInstallation> allocationByProjectAllocation;
	private final Map<String, ProjectDeallocation> deallocationsByProjectAllocationId;
	private final Map<String, Set<ProjectAllocationChunk>> chunksByProjectAllocationId;

	public ProjectAllocationDataSnapshot(Set<ProjectAllocationInstallation> installations, Set<ProjectDeallocation> uninstallations,
	                    Set<ProjectAllocationChunk> chunks) {
		this.allocationByProjectAllocation = installations.stream()
			.collect(toMap(installation -> installation.projectAllocationId, identity()));
		this.deallocationsByProjectAllocationId = uninstallations.stream()
			.collect(toMap(uninstallation -> uninstallation.projectAllocationId, identity()));
		this.chunksByProjectAllocationId = chunks.stream()
			.collect(groupingBy(chunk -> chunk.projectAllocationId, toSet()));
	}

	public Optional<ProjectAllocationInstallation> getAllocation(String projectAllocationId) {
		return Optional.ofNullable(allocationByProjectAllocation.get(projectAllocationId));
	}

	public Set<ProjectAllocationChunk> getChunks(String projectAllocationId) {
		return chunksByProjectAllocationId.getOrDefault(projectAllocationId, Set.of());
	}

	public Optional<ProjectDeallocation> getDeallocationStatus(String projectAllocationId) {
		return Optional.ofNullable(deallocationsByProjectAllocationId.get(projectAllocationId));
	}
}
