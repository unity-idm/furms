/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.invoke.MethodHandles;

@SuppressWarnings("deprecation")
public class AfterCommitLauncher {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	//FIXME It will be fix in FU-238
	public static void runAfterCommit(Runnable agentOperation) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCommit() {
				try {
					agentOperation.run();
				}catch (Exception e){
					LOG.error("This error occurred when trying to send message to site agent", e);
				}
			}
		});
	}
}
