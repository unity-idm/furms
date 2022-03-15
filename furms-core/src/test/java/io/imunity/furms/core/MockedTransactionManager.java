/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class MockedTransactionManager implements PlatformTransactionManager {
	@Override
	public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
		TransactionSynchronizationManager.setActualTransactionActive(true);
		return new DefaultTransactionStatus(null, true, true, false,
			false, null);
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {
		for (TransactionSynchronization transactionSynchronization : TransactionSynchronizationManager
			.getSynchronizations()) {
			transactionSynchronization.afterCompletion(0);
		}
		TransactionSynchronizationManager.clearSynchronization();
		TransactionSynchronizationManager.initSynchronization();
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {
	}

}
