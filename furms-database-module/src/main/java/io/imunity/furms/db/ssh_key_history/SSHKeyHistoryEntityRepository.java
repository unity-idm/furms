/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_history;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface SSHKeyHistoryEntityRepository extends CrudRepository<SSHKeyHistoryEntity, UUID> {

	List<SSHKeyHistoryEntity> findBysiteIdOrderByOriginationTimeDesc(String siteId, Pageable pageable);

	@Modifying
	@Query("delete from ssh_key_history where site_id = :siteId and id not in (select id from ssh_key_history where site_id = :siteId order by origination_time desc  limit :leave)")
	void deleteOldestLeaveOnly(String siteId, int leave);

}
