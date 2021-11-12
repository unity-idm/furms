/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.site_agent_pending_message;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface SiteAgentPendingMessageEntityRepository extends CrudRepository<SiteAgentPendingMessageEntity, UUID> {
	Optional<SiteAgentPendingMessageEntity> findByCorrelationId(UUID correlationId);
	Set<SiteAgentPendingMessageEntity> findAllBySiteExternalId(String siteId);
	@Modifying
	@Query("delete from site_agent_pending_message where correlation_id = :correlation_id")
	void deleteByCorrelationId(@Param("correlation_id") UUID correlationId);
}
