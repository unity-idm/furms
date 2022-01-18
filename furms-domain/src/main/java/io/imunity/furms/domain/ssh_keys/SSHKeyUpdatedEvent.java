/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class SSHKeyUpdatedEvent implements FurmsEvent {
	public final SSHKey oldSSHKey;
	public final SSHKey newSSHKey;

	public SSHKeyUpdatedEvent(SSHKey oldSSHKey, SSHKey newSSHKey) {
		this.oldSSHKey = oldSSHKey;
		this.newSSHKey = newSSHKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeyUpdatedEvent userEvent = (SSHKeyUpdatedEvent) o;
		return Objects.equals(oldSSHKey, userEvent.oldSSHKey) &&
			Objects.equals(newSSHKey, userEvent.newSSHKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldSSHKey, newSSHKey);
	}

	@Override
	public String toString() {
		return "SSHKeyUpdatedEvent{" +
			"newSSHKey='" + newSSHKey + '\'' +
			"oldSSHKey='" + oldSSHKey + '\'' +
			'}';
	}
}
