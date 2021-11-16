/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.branding.layout;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Div;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.Optional;

public class ExtraLayoutPanel extends Div {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ExtraLayoutPanel(final String id, final String filePath) {
		setId(id);
		final Optional<FileSystemResource> panelFile = findPanelFile(filePath);
		if (panelFile.isPresent()) {
			try {
				final Html html = new Html(panelFile.get().getInputStream());
				getElement().appendChild(html.getElement());
			} catch (IOException exception) {
				LOG.error("Could not load panel: " + id, exception);
			}
		}
	}

	private Optional<FileSystemResource> findPanelFile(String filePath) {
		if (StringUtils.isBlank(filePath)) {
			return Optional.empty();
		}
		final FileSystemResource systemFile = new FileSystemResource(filePath);
		if (systemFile.exists() && systemFile.isReadable()) {
			return Optional.of(systemFile);
		}
		final URL resourceUrl = getClass().getResource(filePath);
		if (resourceUrl == null) {
			return Optional.empty();
		}
		final FileSystemResource resourceFile = new FileSystemResource(resourceUrl.getPath());
		return resourceFile.exists() && resourceFile.isReadable()
				? Optional.of(resourceFile)
				: Optional.empty();
	}

}
