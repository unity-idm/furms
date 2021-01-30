/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.user;

import org.springframework.stereotype.Service;

import io.imunity.furms.api.user.UserService;
import io.imunity.furms.domain.users.UserRecord;
import io.imunity.furms.domain.users.UserStatus;

@Service
class UserServiceImpl implements UserService {
	
	@Override
	public UserRecord getUserRecord(String fenixUserId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
	
	
	@Override
	public void setUserStatus(String fenixUserId, UserStatus userStatus) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}

	@Override
	public UserStatus getUserStatus(String fenixUserId) {
		throw new UnsupportedOperationException("Not implemented yet"); // TODO
	}
}
