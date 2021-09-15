/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;

class SSLContextManager {

	static SSLContext createSSLContext(String trustStore, String trustStorePassword) {
		try {
			final Resource resource = new FileSystemResource(trustStore);
			return new SSLContextBuilder()
					.loadTrustMaterial(resource.getFile(), trustStorePassword.toCharArray())
					.build();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
