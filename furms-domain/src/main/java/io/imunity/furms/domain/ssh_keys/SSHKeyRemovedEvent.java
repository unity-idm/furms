/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.ssh_keys;

import io.imunity.furms.domain.FurmsEvent;

import java.util.Objects;

public class SSHKeyRemovedEvent implements FurmsEvent {
	public final SSHKey sshKey;

	public SSHKeyRemovedEvent(SSHKey sshKey) {
		this.sshKey = sshKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SSHKeyRemovedEvent userEvent = (SSHKeyRemovedEvent) o;
		return Objects.equals(sshKey, userEvent.sshKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(sshKey);
	}

	@Override
	public String toString() {
		return "SSHKeyRemovedEvent{" +
			"sshKey='" + sshKey + '\'' +
			'}';
	}
}
