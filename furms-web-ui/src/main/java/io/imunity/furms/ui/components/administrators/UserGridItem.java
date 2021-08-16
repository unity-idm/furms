/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.icon.Icon;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;

import java.util.Objects;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;

public class UserGridItem {
	private final Optional<PersistentId> id;
	private final Optional<String> firstName;
	private final Optional<String> lastName;
	private final UserStatus status;
	private final String email;
	private Icon icon = ANGLE_RIGHT.create();

	public UserGridItem(FURMSUser user){
		this.id = user.id;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.status = user.status;
		this.email = user.email;
	}

	public Optional<PersistentId> getId() {
		return id;
	}

	public Optional<String> getFirstName() {
		return firstName;
	}

	public Optional<String> getLastName() {
		return lastName;
	}

	public UserStatus getStatus() {
		return status;
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
		UserGridItem that = (UserGridItem) o;
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
			", status='" + status + '\'' +
			", email='" + email + '\'' +
			'}';
	}
}
