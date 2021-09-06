/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

public class FurmsClientBuilder {
	private String url;
	private String username;
	private String apiKey;
	private String trustStore;
	private String trustStorePassword;

	FurmsClientBuilder() {
	}

	public FurmsClientBuilder url(String url) {
		this.url = url;
		return this;
	}

	public FurmsClientBuilder username(String username) {
		this.username = username;
		return this;
	}

	public FurmsClientBuilder apiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public FurmsClientBuilder trustStore(String trustStore) {
		this.trustStore = trustStore;
		return this;
	}

	public FurmsClientBuilder trustStorePassword(String trustStorePassword) {
		this.trustStorePassword = trustStorePassword;
		return this;
	}

	public FurmsClient build() {
		return new FurmsClient(url, username, apiKey, trustStore, trustStorePassword);
	}
}
