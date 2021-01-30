/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.cidp;

import java.util.List;

public class GroupMembershipJson {
	public final String name;
	public final List<AttributeJson> attributes;
	
	public GroupMembershipJson(String name, List<AttributeJson> attributes) {
		this.name = name;
		this.attributes = List.copyOf(attributes);
	}
}
