/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

public class InvalidSSHKeyFromOptionException extends RuntimeException {

	public InvalidSSHKeyFromOptionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public InvalidSSHKeyFromOptionException(String msg) {
		super(msg);
	}
}
