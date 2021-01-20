/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.component.icon.Icon;

import java.util.Objects;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;

class UserViewModel {
	public final String id;
	public final String firstName;
	public final String lastName;
	public final String email;
	public Icon icon = ANGLE_RIGHT.create();

	UserViewModel(String id, String firstName, String lastName, String email) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
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
