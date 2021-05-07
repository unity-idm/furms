/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

public class SSHKeyHistoryException extends RuntimeException {

	public final String siteId;
	
	public SSHKeyHistoryException(String error, String siteId) {
		super(error);
		this.siteId = siteId;
	}
}
