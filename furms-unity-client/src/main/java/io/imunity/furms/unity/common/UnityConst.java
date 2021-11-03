/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.common;

import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;

public class UnityConst {
	public static final String IDENTITY_TYPE = "identityType";
	public static final String PERSISTENT_IDENTITY = "persistent";
	public static final String IDENTIFIER_IDENTITY = "identifier";
	public static final String STRING = "string";
	public static final String ENUMERATION = "enumeration";
	public static final String RECURSIVE = "recursive";
	public static final String WITH_PARENTS = "withParents";
	public static final String GROUPS_PATTERNS = "groupsPatterns";
	public static final String ALL_GROUPS_PATTERNS = "/fenix/**/users";

	public static final String GROUP_PATH = "groupPath";
	public static final String ID = "id";
	public static final String COMMUNITY_ID = "communityId";
	public static final String PROJECT_ID = "projectId";
	public static final String ROOT_GROUP_PATH = "rootGroupPath";
	public static final String FURMS_POLICY_ACCEPTANCE_STATE_ATTRIBUTE = "furmsPolicyAgreementState";

	public static final String ROOT_GROUP = "/";
	public static final String FENIX_GROUP = "/fenix";
	public static final String FENIX_PATTERN = "/fenix" + USERS_PATTERN;
	public final static String SITE_PREFIX = "/fenix/sites/";
	public final static String SITE_PATTERN = SITE_PREFIX + "{"+ ID +"}";
	public final static String SITE_USERS_PATTERN = SITE_PREFIX + "{"+ ID +"}" + USERS_PATTERN;
	public final static String COMMUNITY_PREFIX = "/fenix/communities/";
	public final static String COMMUNITY_GROUP_PATTERN = COMMUNITY_PREFIX + "{"+ ID +"}";
	public final static String COMMUNITY_PATTERN = COMMUNITY_GROUP_PATTERN + USERS_PATTERN;
	public final static String PROJECT_PREFIX = "/projects/";
	public final static String PROJECT_GROUP_PATTERN = COMMUNITY_PREFIX + "{"+ COMMUNITY_ID +"}" + PROJECT_PREFIX + "{"+ PROJECT_ID +"}";
	public static final String PROJECT_PATTERN = PROJECT_GROUP_PATTERN + USERS_PATTERN;
}
