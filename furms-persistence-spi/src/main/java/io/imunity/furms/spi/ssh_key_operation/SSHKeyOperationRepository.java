/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJobId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SSHKeyOperationRepository {
	SSHKeyOperationJob findByCorrelationId(CorrelationId id);
	
	SSHKeyOperationJob findBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId);

	SSHKeyOperationJobId create(SSHKeyOperationJob projectInstallationJob);

	void update(SSHKeyOperationJobId id, SSHKeyOperationStatus status, Optional<String> error, LocalDateTime operationTime);

	void delete(SSHKeyOperationJobId id);

	void deleteAll();

	void deleteBySSHKeyIdAndSiteId(SSHKeyId sshkeyId, SiteId siteId);

	List<SSHKeyOperationJob> findBySSHKey(SSHKeyId sshkeyId);

	List<SSHKeyOperationJob> findAll();
	
	List<SSHKeyOperationJob> findByStatus(SSHKeyOperationStatus status);

	void delete(CorrelationId id);
}
