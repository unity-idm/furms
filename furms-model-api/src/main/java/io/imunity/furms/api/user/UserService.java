/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.api.user;

import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

public interface UserService {

	UserRecord getUserRecord(String fenixUserId);

	void setUserStatus(String fenixUserId, UserStatus userStatus);

	UserStatus getUserStatus(String fenixUserId);

}