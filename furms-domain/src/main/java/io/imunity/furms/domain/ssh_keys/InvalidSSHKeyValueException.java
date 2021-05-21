/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

class InvalidSSHKeyValueException extends RuntimeException {

	public InvalidSSHKeyValueException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public InvalidSSHKeyValueException(String msg) {
		super(msg);
	}
}
