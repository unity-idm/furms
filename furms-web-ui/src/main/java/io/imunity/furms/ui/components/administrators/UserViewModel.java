/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.icon.Icon;
import io.imunity.furms.domain.users.User;

import java.util.Objects;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;

public class UserViewModel {
	public final String id;
	public final String firstName;
	public final String lastName;
	public final String email;
	public Icon icon = ANGLE_RIGHT.create();

	public UserViewModel(String id, String firstName, String lastName, String email) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}

	public UserViewModel(User user){
		this.id = user.id;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.email = user.email;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserViewModel that = (UserViewModel) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "UserViewModel{" +
			"id='" + id + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			'}';
	}
}
