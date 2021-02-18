/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.events;

import org.springframework.core.ResolvableType;

public class FurmsEvent<E> implements org.springframework.core.ResolvableTypeProvider{
	public final E entity;
	public final CRUD crud;

	public FurmsEvent(E entity, CRUD crud) {
		this.entity = entity;
		this.crud = crud;
	}

	@Override
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(
			getClass(),
			ResolvableType.forInstance(entity)
		);
	}
}
