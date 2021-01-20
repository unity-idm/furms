/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import io.imunity.furms.domain.users.User;

class UserViewModelMapper {
	static UserViewModel map(User user){
		return new UserViewModel(user.id, user.firstName, user.lastName, user.email);
	}
}
