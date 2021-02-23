/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui;

import com.vaadin.flow.shared.Registration;

public class SynchronizedRegistration implements Registration {
	public final Runnable runnable;

	SynchronizedRegistration(Runnable runnable) {
		this.runnable = runnable;
	}

	public synchronized void remove() {
		runnable.run();
	}
}
