/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.constant;

public enum CommonAttribute{

	FIRSTNAME("firstname"),
	SURNAME("surname"),
	EMAIL("email"),
	FENIX_USER_ID("fenixUserId");

	public final String name;

	CommonAttribute(String name) {
		this.name = name;
	}
}
