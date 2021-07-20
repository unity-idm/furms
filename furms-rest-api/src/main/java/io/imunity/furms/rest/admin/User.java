/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.time.LocalDate;
import java.util.Objects;

import io.imunity.furms.domain.users.FURMSUser;

class User {
	public final String fenixIdentifier;
	public final String title;
	public final String firstname;
	public final String lastname;
	public final String email;
	public final Affiliation affiliation;
	public final String nationality;
	public final String phone;
	public final LocalDate dateOfBirth;
	public final String placeOfBirth;
	public final String postalAddress;
	
	User(String fenixIdentifier, String title, String firstname, String lastname, String email,
			Affiliation affiliation, String nationality, String phone, LocalDate dateOfBirth,
			String placeOfBirth, String postalAddress) {
		this.fenixIdentifier = fenixIdentifier;
		this.title = title;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.affiliation = affiliation;
		this.nationality = nationality;
		this.phone = phone;
		this.dateOfBirth = dateOfBirth;
		this.placeOfBirth = placeOfBirth;
		this.postalAddress = postalAddress;
	}

	User(FURMSUser user) {
		this(user.fenixUserId
				.map(fenixId -> fenixId.id)
				.orElse(null),
				null,
				user.firstName.orElse(null),
				user.lastName.orElse(null),
				user.email,
				new Affiliation(user.firstName.orElse(null), user.email, null, null),
				null, null, null, null, null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return Objects.equals(fenixIdentifier, user.fenixIdentifier)
				&& Objects.equals(title, user.title)
				&& Objects.equals(firstname, user.firstname)
				&& Objects.equals(lastname, user.lastname)
				&& Objects.equals(email, user.email)
				&& Objects.equals(affiliation, user.affiliation)
				&& Objects.equals(nationality, user.nationality)
				&& Objects.equals(phone, user.phone)
				&& Objects.equals(dateOfBirth, user.dateOfBirth)
				&& Objects.equals(placeOfBirth, user.placeOfBirth)
				&& Objects.equals(postalAddress, user.postalAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixIdentifier, title, firstname, lastname, email, affiliation, nationality, phone, dateOfBirth, placeOfBirth, postalAddress);
	}

	@Override
	public String toString() {
		return "User{" +
				"fenixIdentifier='" + fenixIdentifier + '\'' +
				", title='" + title + '\'' +
				", firstname='" + firstname + '\'' +
				", lastname='" + lastname + '\'' +
				", email='" + email + '\'' +
				", affiliation=" + affiliation +
				", nationality='" + nationality + '\'' +
				", phone='" + phone + '\'' +
				", dateOfBirth=" + dateOfBirth +
				", placeOfBirth='" + placeOfBirth + '\'' +
				", postalAddress='" + postalAddress + '\'' +
				'}';
	}
}
