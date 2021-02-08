/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.common;

public class UnityConst {
	public static final String IDENTITY_TYPE = "identityType";
	public static final String PERSISTENT_IDENTITY = "persistent";
	public static final String ENUMERATION = "enumeration";
	public static final String RECURSIVE = "recursive";
	public static final String WITH_PARENTS = "withParents";

	public static final String GROUP_PATH = "groupPath";
	public static final String ID = "id";
	public static final String COMMUNITY_ID = "communityId";
	public static final String PROJECT_ID = "projectId";
	public static final String ROOT_GROUP_PATH = "rootGroupPath";

	public static final String ROOT_GROUP = "/";
	public static final String FENIX_GROUP = "/fenix";
	public static final String FENIX_PATTERN = "/fenix/users";
	public final static String SITE_PATTERN = "/fenix/sites/{"+ ID +"}";
	public final static String COMMUNITY_PREFIX = "/fenix/communities/";
	public final static String COMMUNITY_PATTERN = COMMUNITY_PREFIX + "{"+ ID +"}";
	public final static String PROJECT_GROUP_PATTERN = COMMUNITY_PREFIX + "{"+ COMMUNITY_ID +"}/projects/{"+ PROJECT_ID +"}";
	public static final String PROJECT_PATTERN = PROJECT_GROUP_PATTERN + "/users";
}
