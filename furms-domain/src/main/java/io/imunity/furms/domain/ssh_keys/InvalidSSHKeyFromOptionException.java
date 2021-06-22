/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

public class InvalidSSHKeyFromOptionException extends RuntimeException {

	public enum ErrorType {
		CIDR_MASK, INVALID_HOST, WILDCARD_WITH_TLD, WILDCARD, NON_ROUTEABLE_HOST, WILDCARD_IN_ADDRESS
	}

	public final String fromOption;
	public final ErrorType type;

	public InvalidSSHKeyFromOptionException(String msg, Throwable cause, String fromOption, ErrorType type) {
		super(msg, cause);
		this.fromOption = fromOption;
		this.type = type;

	}

	public InvalidSSHKeyFromOptionException(String msg, String fromOption, ErrorType type) {
		super(msg);
		this.fromOption = fromOption;
		this.type = type;
	}
}
