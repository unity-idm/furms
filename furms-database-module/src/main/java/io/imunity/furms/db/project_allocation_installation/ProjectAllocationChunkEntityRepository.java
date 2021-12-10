/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProjectAllocationChunkEntityRepository extends CrudRepository<ProjectAllocationChunkEntity, UUID> {
	@Query("select pac.* " +
		"from project_allocation_chunk pac " +
		"join project_allocation pa on pa.id = pac.project_allocation_id " +
		"where pa.project_id = :id")
	Set<ProjectAllocationChunkEntity> findAllByProjectId(@Param("id") UUID projectId);

	Set<ProjectAllocationChunkEntity> findAllByProjectAllocationId(UUID projectAllocationId);

	Optional<ProjectAllocationChunkEntity> findByProjectAllocationIdAndChunkId(UUID projectAllocationId, String chunkId);
}
