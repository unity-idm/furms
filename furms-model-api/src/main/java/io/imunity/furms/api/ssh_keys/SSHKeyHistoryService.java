/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */


package io.imunity.furms.api.ssh_keys;

import java.util.List;

import io.imunity.furms.domain.ssh_keys.SSHKeyHistory;

public interface SSHKeyHistoryService {

	void create(SSHKeyHistory sshKeyHistory);
	
	public List<SSHKeyHistory> findLastBySSHKeyIdLimitTo(String siteId, int limit);

	void deleteOldestLeaveOnly(String siteId, int leave);
}
