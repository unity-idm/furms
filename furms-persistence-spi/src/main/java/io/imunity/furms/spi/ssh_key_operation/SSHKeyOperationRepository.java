/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SSHKeyOperationRepository {
	SSHKeyOperationJob findByCorrelationId(CorrelationId id);
	
	SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	String create(SSHKeyOperationJob projectInstallationJob);

	void update(String id, SSHKeyOperationStatus status, Optional<String> error, LocalDateTime operationTime);

	void delete(String id);

	void deleteAll();

	void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	List<SSHKeyOperationJob> findBySSHKey(String sshkeyId);

	List<SSHKeyOperationJob> findAll();
	
	List<SSHKeyOperationJob> findByStatus(SSHKeyOperationStatus status);

	void delete(CorrelationId id);
}
