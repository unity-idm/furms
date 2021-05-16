/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.UI;

public class InvocationContext implements Serializable {

	private static final Logger LOG = LoggerFactory.getLogger(InvocationContext.class);

	private static final int ZONE_INIT_TIMEOUT = 30;

	private final Future<ZoneId> zone;

	public InvocationContext(Future<ZoneId> zone) {
		this.zone = zone;
	}

	public static void setCurrent(InvocationContext context) {
		UI.getCurrent().getSession().setAttribute(InvocationContext.class, context);
	}

	public static InvocationContext getCurrent() {
		InvocationContext ret = UI.getCurrent().getSession().getAttribute(InvocationContext.class);
		if (ret == null)
			throw new EmptyInvocationContextException("The current call has no invocation context set");
		return ret;
	}

	public ZoneId getZone() {
		try {
			return zone.get(ZONE_INIT_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOG.error("Can not get user zone");
		}
		return ZoneId.of("UTC");
	}

	public static void init(UI ui) {
		CompletableFuture<ZoneId> zone = new CompletableFuture<>();
		ui.getPage().retrieveExtendedClientDetails(cd -> {
			zone.complete(ZoneId.of(cd.getTimeZoneId()));
		});

		setCurrent(new InvocationContext(zone));
	}
}
