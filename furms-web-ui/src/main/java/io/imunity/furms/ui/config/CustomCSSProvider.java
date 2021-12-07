/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import com.vaadin.flow.component.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static org.apache.logging.log4j.util.Strings.isEmpty;
import static org.springframework.util.ResourceUtils.FILE_URL_PREFIX;

@Configuration
public class CustomCSSProvider implements WebMvcConfigurer {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Resource externalCSSResource;

	public CustomCSSProvider(@Value("${furms.front.layout.styles.custom}") Resource externalCSSResource) {
		this.externalCSSResource = externalCSSResource;
	}

	@Override
	public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		if (externalCSSResource != null) {
			try {
				if (isCustomCssFileAvailable()) {
					final String extraStyleResourceLocation = createResourceLocationsFrom(externalCSSResource);
					registry.addResourceHandler("/extra-style/{filename:(?:" + externalCSSResource.getFilename() + ")}")
							.addResourceLocations(extraStyleResourceLocation);
				} else {
					LOG.error("Custom CSS File is not available to load and configure ({})", externalCSSResource);
				}
			} catch (IOException exception) {
				LOG.error("Could not load custom CSS file: ", exception);
			}
		}
	}

	public void initAndAttach(final UI uiToAttachStyle) {
		try {
			if (externalCSSResource != null && isCustomCssFileAvailable()) {
				uiToAttachStyle.getPage().addStyleSheet("/extra-style/" + externalCSSResource.getFilename());
			}
		} catch (final IOException exception) {
			LOG.error("Could not load custom CSS file: ", exception);
		}
	}

	private boolean isCustomCssFileAvailable() throws IOException {
		return !isEmpty(externalCSSResource.getURL().getPath())
				&& externalCSSResource.exists()
				&& externalCSSResource.isReadable();
	}

	private String createResourceLocationsFrom(Resource externalCSSResource) throws IOException {
		final String prefix = !externalCSSResource.getURL().getPath().startsWith(FILE_URL_PREFIX)
				? FILE_URL_PREFIX : "";
		final String suffix = !externalCSSResource.getFile().getParent().endsWith("/")
				? "/" : "";
		return prefix + externalCSSResource.getFile().getParent() + suffix;
	}
}
