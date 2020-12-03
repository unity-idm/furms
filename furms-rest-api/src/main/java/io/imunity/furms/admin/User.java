/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.admin;

import java.time.LocalDate;

class User {
	final String fenixIdentifier;
	final String title;
	final String firstname;
	final String lastname;
	final String email;
	final Affiliation affiliation;
	final String nationality;
	final String phone;
	final LocalDate dateOfBirth;
	final String placeOfBirth;
	final String postalAddress;
	
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
}
