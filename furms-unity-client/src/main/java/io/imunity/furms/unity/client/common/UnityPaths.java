/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.common;

import static io.imunity.furms.unity.client.common.UnityConst.ID;

public class UnityPaths {
	public final static String GROUP_BASE = "/group/";
	public final static String ENTITY_BASE = "/entity/";
	public final static String GROUP_MEMBERS = "/group-members/";
	public final static String META = "/meta";
	public final static String USERS_PATTERN = "/users";
	public final static String ATTRIBUTE_PATTERN = "/attribute";
	public final static String ATTRIBUTES_PATTERN = "/attributes";
	public final static String GROUP = "group";
	public final static String GROUP_ATTRIBUTES = "entity/{"+ ID +"}/groups/attributes";
	public final static String ENTITY_GROUPS = "entity/{"+ ID +"}/groups";
}
