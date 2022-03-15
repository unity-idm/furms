/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.invoke.MethodHandles;

@Component
public class AfterCommitLauncher {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@TransactionalEventListener
	void runAfterCommit(InvokeAfterCommitEvent invokeAfterCommitEvent) {
		try {
			invokeAfterCommitEvent.operation.run();
		} catch (Exception e) {
			LOG.error("This error occurred when trying to send message to site agent", e);
		}
	}
}
