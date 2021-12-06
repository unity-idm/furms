/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.layout;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.apache.logging.log4j.util.Strings.isEmpty;

public class ExtraLayoutPanel extends Div {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ExtraLayoutPanel(final String id, final Resource panelFile) {
		setId(id);
		if (isPanelFileExists(panelFile)) {
			try {
				if (panelFile.isReadable()) {
					final Html html = new Html(panelFile.getInputStream());
					getElement().appendChild(html.getElement());
				} else {
					LOG.error("Panel File exists but couldn't be read: {} , files is unreadable", id);
				}
			} catch (IOException | IllegalArgumentException exception) {
				LOG.error("Could not load panel: " + id, exception);
			}
		}
	}

	private boolean isPanelFileExists(final Resource panelFile) {
		try {
			return panelFile != null
					&& !isEmpty(panelFile.getURL().getPath())
					&& panelFile.exists();
		} catch (IOException exception) {
			return false;
		}
	}

}
