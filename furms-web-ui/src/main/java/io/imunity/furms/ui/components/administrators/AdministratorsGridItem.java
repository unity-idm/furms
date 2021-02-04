/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.icon.Icon;
import io.imunity.furms.domain.users.User;

import java.util.Objects;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;

public class AdministratorsGridItem {
	private final String id;
	private final String firstName;
	private final String lastName;
	private final String email;
	private Icon icon = ANGLE_RIGHT.create();

	public AdministratorsGridItem(User user){
		this.id = user.id;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.email = user.email;
	}

	public String getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmail() {
		return email;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AdministratorsGridItem that = (AdministratorsGridItem) o;
		return id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "AdministratorsGridItem{" +
			"id='" + id + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			'}';
	}
}
