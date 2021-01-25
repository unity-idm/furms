/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class VaadinExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static <T> Optional<T> handleExceptions(Supplier<T> supplier){
		try {
			return Optional.ofNullable(supplier.get());
		}catch (Exception e){
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("base.error.message"));
			return Optional.empty();
		}
	}

	public static void handleExceptions(Runnable runnable){
		handleExceptions(() -> {
			runnable.run();
			return null;
		});
	}

}
