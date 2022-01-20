/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface ResourceCreditEntityRepository extends CrudRepository<ResourceCreditEntity, UUID> {

	Set<ResourceCreditEntity> findAllBySiteId(UUID siteId);

	Set<ResourceCreditEntity> findAllByResourceTypeId(UUID resourceTypeId);

	@Query("SELECT rc.* " +
			"FROM resource_credit rc " +
			"JOIN site s ON rc.site_id = s.id " +
			"WHERE (UPPER(rc.name) LIKE UPPER(CONCAT('%', :name, '%')) " +
			"        OR UPPER(s.name) LIKE UPPER(CONCAT('%', :name, '%')))")
	Set<ResourceCreditEntity> findAllByNameOrSiteName(@Param("name") String name);

	boolean existsByNameAndSiteId(String name, UUID siteId);

	boolean existsBySiteId(UUID siteId);

	boolean existsByResourceTypeId(UUID resourceTypeId);

	boolean existsByResourceTypeIdIn(Collection<UUID> resourceTypeIds);
}
