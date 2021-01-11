/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.constant;

public final class RoutesConst {
	public static final String PUBLIC_URL = "/front/public/";
	public static final String LOGIN_URL = "/front/public/login";
	public static final String LOGIN_ERROR_URL = "/front/public/login/error";
	public static final String LOGIN_SUCCESS_URL = "/front/choose/role";
	public static final String LOGOUT_URL = "/logout";
	public static final String LOGOUT_SUCCESS_URL = "/front/public/logout";
	public static final String FRONT = "/front";

	public static final String PUBLIC_URL = "/public";

	public static final String LOGIN_URL = PUBLIC_URL + "/login";
	public static final String LOGOUT_URL = PUBLIC_URL + "/logout";
	public static final String LOGIN_ERROR_URL = PUBLIC_URL + "/login-error";

	public static final String FRONT_LOGOUT_URL = FRONT + LOGOUT_URL;

	public static final String FENIX_ADMIN_LANDING_PAGE = "fenix/admin/sites";
	public static final String LOGIN_SUCCESS_URL = FRONT + "/" + FENIX_ADMIN_LANDING_PAGE;

	public static final String REGISTRATION_ID = "/unity";
	public static final String AUTH_REQ_BASE_URL = "/oauth2/authorization";
	public static final String AUTH_REQ_PARAM_URL = "/oauth2/authorization/param";

	public static final String FENIX_BASE_URL = "fenix/admin/sites";
	public static final String SITE_BASE_URL = "site/admin/policy/documents";
	public static final String SITE_SUPPORT_BASE_URL = "site/support/signed/policies";
	public static final String COMMUNITY_BASE_URL = "community/admin/dashboard";
	public static final String PROJECT_BASE_URL = "project/admin/users";
	public static final String USER_BASE_URL = "users/settings/profile";

	public static final String PROXY_AUTH_PARAM = "showSignInOptions";
}
