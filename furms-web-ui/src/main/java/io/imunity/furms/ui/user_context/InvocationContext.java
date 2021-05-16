/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.vaadin.flow.component.UI;

public class InvocationContext implements Serializable {

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
			throw new RuntimeException("The current call has no invocation context set");
		return ret;
	}

	public ZoneId getZone() {
		try {
			return zone.get(5, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException("Can not get user zone");
		}
	}

	public static void init() {
		CompletableFuture<ZoneId> zone = new CompletableFuture<>();
		UI.getCurrent().getPage().retrieveExtendedClientDetails(cd -> {
			zone.complete(ZoneId.of(cd.getTimeZoneId()));
		});

		setCurrent(new InvocationContext(zone));
	}

}
