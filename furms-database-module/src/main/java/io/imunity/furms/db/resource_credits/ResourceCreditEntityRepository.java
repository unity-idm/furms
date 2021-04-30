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
			"WHERE (end_time > now() OR (end_time <= now()) = :includedExpired) " +
			"   AND (UPPER(rc.name) LIKE UPPER(CONCAT('%', :name, '%')) " +
			"        OR UPPER(s.name) LIKE UPPER(CONCAT('%', :name, '%')))")
	Stream<ResourceCreditEntity> findAllByNameAndIncludedExpired(@Param("name") String name,
	                                                             @Param("includedExpired") boolean includedExpired);
	boolean existsByName(String name);
	boolean existsBySiteId(UUID siteId);
	boolean existsByResourceTypeId(UUID resourceTypeId);
	boolean existsByResourceTypeIdIn(Collection<UUID> resourceTypeIds);
}
