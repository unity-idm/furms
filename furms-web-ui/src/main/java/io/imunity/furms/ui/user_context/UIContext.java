/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UIContext {
	
	private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("UTC");

	private static final Logger LOG = LoggerFactory.getLogger(UIContext.class);

	private static final int ZONE_INIT_TIMEOUT_S = 3;

	private final Future<ZoneId> zone;

	public UIContext(Future<ZoneId> zone) {
		this.zone = zone;
	}

	public static UIContext getCurrent() {
		UI ui = UI.getCurrent();
		UIContext ret = ComponentUtil.getData(ui, UIContext.class);
		if (ret == null) {
			LOG.debug("Recreate invocation context");
			init(ui);
			ret = ComponentUtil.getData(ui, UIContext.class);
		}
		return ret;
	}

	public ZoneId getZone() {
		try {
			return zone.get(ZONE_INIT_TIMEOUT_S, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			LOG.warn("Can not get user zone, falling back to UTC", e);
		}
		return DEFAULT_ZONE_ID;
	}
	
	public static void init(UI ui) {
		LOG.debug("Initializing zone");
		CompletableFuture<ZoneId> zone = new CompletableFuture<>();
		ui.getPage().retrieveExtendedClientDetails(cd -> {
			if (cd != null) {
				String retrivedTZ = cd.getTimeZoneId();
				LOG.debug("Zone initialized to {}", retrivedTZ);
				zone.complete(retrivedTZ == null ? DEFAULT_ZONE_ID : ZoneId.of(retrivedTZ));
			} else {
				LOG.debug("Can't retrieve extended client details");
				zone.complete(DEFAULT_ZONE_ID);
			}
		});

		new UIContext(zone).setAsCurrent(ui);
	}
	
	private void setAsCurrent(UI ui) {
		ComponentUtil.setData(ui, UIContext.class, this);
	}
}
