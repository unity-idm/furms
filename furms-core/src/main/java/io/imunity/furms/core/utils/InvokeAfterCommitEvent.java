/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import java.util.Objects;

public class InvokeAfterCommitEvent {
	public final Runnable operation;

	public InvokeAfterCommitEvent(Runnable operation) {
		this.operation = operation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InvokeAfterCommitEvent that = (InvokeAfterCommitEvent) o;
		return Objects.equals(operation, that.operation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(operation);
	}

	@Override
	public String toString() {
		return "InvokeAfterCommitEvent{" +
			"runnable=" + operation +
			'}';
	}
}
