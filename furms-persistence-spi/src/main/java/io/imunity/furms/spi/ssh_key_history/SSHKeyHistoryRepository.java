/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_history;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;

public interface SSHKeyHistoryRepository {
	
	List<SSHKeyHistory> findBySiteIdAndOwnerIdLimitTo(String siteId, String ownerId, int limit);

	String create(SSHKeyHistory sshKeyHistory);
	
	void deleteOldestLeaveOnly(String siteId, String ownerId, int leave);
}
