/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import javax.validation.Valid;
import java.util.Objects;


public class Payload<T extends Body> implements ResolvableTypeProvider {
	public final Header header;
	@Valid
	public final T body;

	@SuppressWarnings("unchecked")
	@JsonCreator
	public Payload(Header header, Body body) {
		this.header = header;
		this.body = (T)body;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Payload<?> payload = (Payload<?>) o;
		return Objects.equals(header, payload.header) &&
			Objects.equals(body, payload.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(header, body);
	}

	@Override
	public String toString() {
		return "Payload{" +
			"header=" + header +
			", body=" + body +
			'}';
	}

	@Override
	@JsonIgnore
	public ResolvableType getResolvableType() {
		return ResolvableType.forClassWithGenerics(
			getClass(),
			ResolvableType.forInstance(this.body)
		);
	}
}
