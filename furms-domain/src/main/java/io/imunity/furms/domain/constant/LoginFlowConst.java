/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.constant;

public final class LoginFlowConst {
	
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

	public static final String PROXY_AUTH_PARAM = "showSignInOptions";
}
