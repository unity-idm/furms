/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.domain.constant;

public enum CommonAttribute{

	FIRSTNAME("firstname"),
	SURNAME("surname"),
	EMAIL("email");

	public final String name;

	private CommonAttribute(String name)
	{
		this.name = name;
	}
}
