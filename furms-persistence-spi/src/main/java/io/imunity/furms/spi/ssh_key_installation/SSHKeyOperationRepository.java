/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_installation;

import java.util.List;
import java.util.Optional;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationJob;
import io.imunity.furms.domain.ssh_key_operation.SSHKeyOperationStatus;

public interface SSHKeyOperationRepository {
	SSHKeyOperationJob findByCorrelationId(CorrelationId id);
	
	SSHKeyOperationJob findBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	String create(SSHKeyOperationJob projectInstallationJob);

	String update(String id, SSHKeyOperationStatus status, Optional<String> error);

	void delete(String id);

	void deleteAll();

	void deleteBySSHKeyIdAndSiteId(String sshkeyId, String siteId);

	List<SSHKeyOperationJob> findBySSHKey(String sshkeyId);
}
