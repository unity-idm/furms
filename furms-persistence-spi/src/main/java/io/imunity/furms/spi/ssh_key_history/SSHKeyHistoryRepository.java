/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_history;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;

public interface SSHKeyHistoryRepository {
	
	List<SSHKeyHistory>findLastBySSHKeyIdLimitTo(String siteId, int limit);

	String create(SSHKeyHistory sshKeyHistory);
	
	void deleteOldestLeaveOnly(String siteId, int leave);
}
