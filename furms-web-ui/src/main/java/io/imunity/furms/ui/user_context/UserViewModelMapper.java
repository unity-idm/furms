/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.user_context;

import io.imunity.furms.domain.users.User;

public class UserViewModelMapper {
	public static UserViewModel map(User user){
		return new UserViewModel(user.id, user.firstName, user.lastName, user.email);
	}
}
