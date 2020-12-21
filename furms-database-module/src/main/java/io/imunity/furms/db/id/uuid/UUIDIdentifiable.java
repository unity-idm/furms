/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.db.id.uuid;

import org.springframework.data.annotation.Id;

import java.util.UUID;

public abstract class UUIDIdentifiable {

	@Id
	protected UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
