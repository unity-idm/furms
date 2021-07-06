/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.policy_documents;

public enum PolicyFileType {
	PDF("pdf", "application/pdf");

	PolicyFileType(String extension, String type) {
		this.extension = extension;
		this.type = type;
	}

	public final String extension;
	public final String type;
}
