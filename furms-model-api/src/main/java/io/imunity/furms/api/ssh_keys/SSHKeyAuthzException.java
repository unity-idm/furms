/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.ssh_keys;

public class SSHKeyAuthzException extends RuntimeException {

	public SSHKeyAuthzException(String error) {
		super(error);
	}
}
