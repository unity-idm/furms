/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.ssh_key_history;

import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;

import java.util.List;

public interface SSHKeyHistoryRepository {
	
	List<SSHKeyHistory> findBySiteIdAndOwnerIdLimitTo(SiteId siteId, String ownerId, int limit);

	String create(SSHKeyHistory sshKeyHistory);
	
	void deleteOldestLeaveOnly(SiteId siteId, String ownerId, int leave);

	void deleteLatest(SiteId siteId, String ownerId);
}
