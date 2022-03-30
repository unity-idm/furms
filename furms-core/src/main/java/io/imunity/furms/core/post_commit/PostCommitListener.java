/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.post_commit;

import io.imunity.furms.core.utils.InvokeAfterCommitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.lang.invoke.MethodHandles;

@Component
class PostCommitListener {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@TransactionalEventListener
	void onInvokeAfterCommitEvent(InvokeAfterCommitEvent invokeAfterCommitEvent) {
		try {
			invokeAfterCommitEvent.operation.run();
		} catch (Exception e) {
			LOG.error("This error occurred when trying to invoke operation after transaction commit", e);
		}
	}
}
