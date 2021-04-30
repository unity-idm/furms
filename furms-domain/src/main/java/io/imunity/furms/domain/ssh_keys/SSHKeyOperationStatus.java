/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

public enum SSHKeyOperationStatus {
	SEND, ACK, DONE, FAILED;
	
	public boolean inProgress()
	{
		return !DONE.equals(this) && !FAILED.equals(this);
	}
	
}
