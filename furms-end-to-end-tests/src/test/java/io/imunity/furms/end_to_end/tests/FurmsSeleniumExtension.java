/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FurmsSeleniumExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource{
	private static final Lock LOCK = new ReentrantLock();
	private static volatile boolean started = false;

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		LOCK.lock();
		try {
			if (!started) {
				started = true;
				Setuper.main(new String[]{});
			}
		}
        finally {
			LOCK.unlock();
		}
	}

	@Override
	public void close() {
		// Your "after all tests" logic goes here
	}
}
