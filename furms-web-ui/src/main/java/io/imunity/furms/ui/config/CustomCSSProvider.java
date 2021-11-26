/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import com.vaadin.flow.component.UI;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

import static org.apache.logging.log4j.util.Strings.isEmpty;

@Component
public class CustomCSSProvider {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final static String CUSTOM_CSS_PATH = "META-INF/resources/frontend/styles/custom-dynamic.css";
	private final static String CUSTOM_CSS_FRONTEND_PATH = "./styles/custom-dynamic.css";

	private final FileSystemResource externalCSSResource;
	private final ClassPathResource customCSSResource;

	public CustomCSSProvider(@Value("${furms.front.layout.styles.custom}") FileSystemResource externalCSSResource,
	                         @Value(CUSTOM_CSS_PATH) ClassPathResource customCSSResource) {
		this.externalCSSResource = externalCSSResource;
		this.customCSSResource = customCSSResource;
	}

	public void initAndAttach(final UI uiToAttachStyle) {
		try {
			if (isExternalResourceExists()) {
				writeToCSS(new String(externalCSSResource.getInputStream().readAllBytes()));
			} else {
				clearCSS();
			}
			uiToAttachStyle.getPage().addStyleSheet(CUSTOM_CSS_FRONTEND_PATH);
		} catch (final IOException exception) {
			LOG.error("Could not load custom CSS file: ", exception);
		}
	}

	private boolean isExternalResourceExists() {
		return externalCSSResource != null
				&& !isEmpty(externalCSSResource.getPath())
				&& externalCSSResource.exists()
				&& externalCSSResource.isReadable();
	}

	private void clearCSS() throws IOException {
		writeToCSS("");
	}

	private void writeToCSS(String content) throws IOException {
		FileUtils.write(
				customCSSResource.getFile(),
				content,
				StandardCharsets.UTF_8);
	}
}
