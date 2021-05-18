/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.ssh_key_installation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

interface InstalledSSHKeyEntityRepository extends CrudRepository<InstalledSSHKeyEntity, UUID> {

	Optional<InstalledSSHKeyEntity> findBySshkeyIdAndSiteId(UUID sshkeyId, UUID siteId);

	@Modifying
	@Query("delete from installed_ssh_key where sshkey_Id = :sshkey_id and site_Id = :site_id")
	void deleteBySshKeyIdAndSiteId(@Param("sshkey_id") UUID sshkeyId, @Param("site_id") UUID siteId);

	List<InstalledSSHKeyEntity> findBySshkeyId(UUID sshkeyId);

}
