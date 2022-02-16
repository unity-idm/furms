/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components.layout;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlRequestListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.Optional;

import static java.lang.String.format;

@SpringComponent
class CustomStylesInitializer implements VaadinServiceInitListener
{
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	private final Resource externalCSSResource;
	
	CustomStylesInitializer(@Value("${furms.front.layout.styles.custom}") Resource externalCSSResource) {
		this.externalCSSResource = externalCSSResource;
	}
	
	@Override
	public void serviceInit(ServiceInitEvent serviceInitEvent) {
		serviceInitEvent.addIndexHtmlRequestListener(new CustomStylesInjector(externalCSSResource));
	}
	
	private static class CustomStylesInjector implements IndexHtmlRequestListener {

		private final CustomStylesContentProvider contentProvider;
		
		CustomStylesInjector(Resource externalCSSResource) {
			this.contentProvider = new CustomStylesContentProvider(externalCSSResource);
		}

		@Override
		public void modifyIndexHtmlResponse(IndexHtmlResponse indexHtmlResponse) {
			contentProvider.getCustomStyles().ifPresent(customStyles -> {
				Document document = indexHtmlResponse.getDocument();
				org.jsoup.nodes.Element head = document.head();
				head.appendChild(createCustomStyle(document, customStyles));
			});
		}

		private Element createCustomStyle(Document document, String customStyles) {
			Element customStyle = document.createElement("custom-style");
			Element style = document.createElement("style");
			customStyle.appendChild(style);
			style.appendText(customStyles);
			return customStyle;
		}
	}
	
	private static class CustomStylesContentProvider {

		private final Resource externalCSSResource;

		CustomStylesContentProvider(Resource externalCSSResource) {
			this.externalCSSResource = externalCSSResource;
		}
		
		private Optional<String> getCustomStyles() {
			
			if (isCustomCssFileAvailable()) {
				String msg = null;
				try {
					msg = StreamUtils.copyToString(externalCSSResource.getInputStream(), Charset.defaultCharset());
				} catch (IOException exception) {
					LOG.error(format("Could not read custom CSS file: %s", externalCSSResource.getFilename()), 
							exception);
				}
				return Optional.ofNullable(msg);
			}
			return Optional.empty();
		}
		
		private boolean isCustomCssFileAvailable() {
			
			if (externalCSSResource == null) {
				LOG.debug("Custom style is not configured.", externalCSSResource);
				return false;
			}
			
			if (!externalCSSResource.exists()) {
				LOG.error("Could not load custom styles: file does not exists, {}.", externalCSSResource);
				return false;
			}
			
			if (!externalCSSResource.isReadable()) {
				LOG.error("Could not load custom styles: unable to read file content, {}.", externalCSSResource);
				return false;
			}
			
			return true;
		}
	}
}
