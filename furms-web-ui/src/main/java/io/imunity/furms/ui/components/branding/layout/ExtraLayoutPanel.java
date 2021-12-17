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
		if (panelFile != null) {
			try {
				if (isPanelFileReadable(panelFile)) {
					final Html html = new Html(panelFile.getInputStream());
					getElement().appendChild(html.getElement());
				} else {
					LOG.error("Configured Panel File: {}, couldn't be read, file is unreachable", 
							panelFile.getURL().getPath());
				}
			} catch (IOException | IllegalArgumentException exception) {
				LOG.error("Could not load panel: " + id, exception);
			}
		}
	}

	private boolean isPanelFileReadable(final Resource panelFile) {
		try {
			return !isEmpty(panelFile.getURL().getPath())
					&& panelFile.exists()
					&& panelFile.isReadable();
		} catch (IOException exception) {
			return false;
		}
	}

}
