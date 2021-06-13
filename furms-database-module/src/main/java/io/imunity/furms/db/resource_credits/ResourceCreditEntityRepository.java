/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_credits;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public interface ResourceCreditEntityRepository extends CrudRepository<ResourceCreditEntity, UUID> {

	Stream<ResourceCreditEntity> findAllBySiteId(UUID siteId);

	Stream<ResourceCreditEntity> findAllByResourceTypeId(UUID resourceTypeId);

	@Query("SELECT rc.* " +
			"FROM resource_credit rc " +
			"JOIN site s ON rc.site_id = s.id " +
			"WHERE (UPPER(rc.name) LIKE UPPER(CONCAT('%', :name, '%')) " +
			"        OR UPPER(s.name) LIKE UPPER(CONCAT('%', :name, '%')))")
	Stream<ResourceCreditEntity> findAllByNameOrSiteName(@Param("name") String name);

	boolean existsByNameAndSiteId(String name, UUID siteId);

	boolean existsBySiteId(UUID siteId);

	boolean existsByResourceTypeId(UUID resourceTypeId);

	boolean existsByResourceTypeIdIn(Collection<UUID> resourceTypeIds);
}
