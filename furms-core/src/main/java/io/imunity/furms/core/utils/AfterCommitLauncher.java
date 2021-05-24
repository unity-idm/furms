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
import java.util.List;

public class AfterCommitLauncher {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


	public static void runAfterCommit(List<Runnable> agentOperations) {
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
			@Override
			public void afterCommit() {
				for (Runnable agentOperation : agentOperations) {
					try {
						agentOperation.run();
					}catch (Exception e){
						LOG.error("This error occurred when trying to send message to site agent", e);
					}
				}
			}
		});
	}

	public static void runAfterCommit(Runnable agentOperation) {
		runAfterCommit(List.of(agentOperation));
	}
}
