/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_operation;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SSHKeyOperationJobEntityRepository extends CrudRepository<SSHKeyOperationJobEntity, UUID> {
	SSHKeyOperationJobEntity findByCorrelationId(UUID correlationId);

	SSHKeyOperationJobEntity findBySshkeyIdAndSiteId(UUID sshkeyId, UUID siteId);

	@Modifying
	@Query("delete from  ssh_key_operation_job where sshkey_Id = :sshkey_id and site_Id = :site_id")
	void deleteBySshKeyIdAndSiteId(@Param("sshkey_id") UUID sshkeyId,
			@Param("site_id") UUID siteId);

	List<SSHKeyOperationJobEntity> findBySshkeyId(String sshkeyId);

	List<SSHKeyOperationJobEntity> findByStatus(String status);

}
