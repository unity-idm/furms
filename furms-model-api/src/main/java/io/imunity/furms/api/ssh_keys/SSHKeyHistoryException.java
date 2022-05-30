/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;

public class SSHKeyHistoryException extends RuntimeException {

	public final SiteId siteId;
	
	public SSHKeyHistoryException(String error, SiteId siteId) {
		super(error);
		this.siteId = siteId;
	}
}
