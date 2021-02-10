/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
class SslContextManager {
	@Value("${unity.client.ssl.trust-store}")
	private Resource keyStore;
	@Value("${unity.client.ssl.trust-store-password}")
	private String keyStorePassword;

	SSLContext getSslContextForRestTemplate() {
		try {
			return new SSLContextBuilder()
				.loadTrustMaterial(keyStore.getFile(), keyStorePassword.toCharArray())
				.build();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	SslContext getSslContextForWebClient(){
		try {
			FileInputStream trustStoreInputStream = new FileInputStream(keyStore.getFile());
			KeyStore trustStore = KeyStore.getInstance("jks");
			trustStore.load(trustStoreInputStream, keyStorePassword.toCharArray());
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(trustStore);
			return SslContextBuilder.forClient()
				.trustManager(trustManagerFactory)
				.build();
		} catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
