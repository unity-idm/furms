/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.admin;

class Affiliation {

	final String name;
	final String email;
	final String country;
	final String postalAddress;
	
	Affiliation(String name, String email, String country, String postalAddress) {
		this.name = name;
		this.email = email;
		this.country = country;
		this.postalAddress = postalAddress;
	}
}
