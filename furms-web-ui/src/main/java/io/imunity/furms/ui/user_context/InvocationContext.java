/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;

public class InvocationContext {

	private static final Logger LOG = LoggerFactory.getLogger(InvocationContext.class);

	private static final int ZONE_INIT_TIMEOUT_S = 20;

	private final Future<ZoneId> zone;

	public InvocationContext(Future<ZoneId> zone) {
		this.zone = zone;
	}

	public void setAsCurrent() {
		UI ui = UI.getCurrent();
		ComponentUtil.setData(ui, InvocationContext.class, this);
	}

	public static InvocationContext getCurrent() {
		UI ui = UI.getCurrent();
		InvocationContext ret = ComponentUtil.getData(ui, InvocationContext.class);
		if (ret == null)
			throw new EmptyInvocationContextException("The current call has no invocation context set");
		return ret;
	}

	public ZoneId getZone() {
		try {
			return zone.get(ZONE_INIT_TIMEOUT_S, TimeUnit.SECONDS);
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

		new InvocationContext(zone).setAsCurrent();
	}
}
