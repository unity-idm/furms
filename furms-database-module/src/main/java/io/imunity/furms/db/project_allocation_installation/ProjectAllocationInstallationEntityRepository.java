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

public interface ProjectAllocationInstallationEntityRepository extends CrudRepository<ProjectAllocationInstallationEntity, UUID> {
	@Query("select pai.* " +
		"from project_allocation_installation pai " +
		"join project_allocation pa on pa.id = pai.project_allocation_id " +
		"where pa.project_id = :project_id and pai.site_id = :site_id")
	Set<ProjectAllocationInstallationEntity> findAllByProjectIdAndSiteId(@Param("project_id") UUID projectId, @Param("site_id") UUID siteId);

	@Query("select pai.* " +
		"from project_allocation_installation pai " +
		"join project_allocation pa on pa.id = pai.project_allocation_id " +
		"where pa.project_id = :project_id")
	Set<ProjectAllocationInstallationEntity> findAllByProjectId(@Param("project_id") UUID projectId);

	Optional<ProjectAllocationInstallationEntity> findByProjectAllocationId(UUID projectAllocationId);

	Optional<ProjectAllocationInstallationEntity> findByCorrelationId(UUID correlationId);
}
