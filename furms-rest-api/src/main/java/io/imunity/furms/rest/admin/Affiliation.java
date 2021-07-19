/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class Affiliation {

	public final String name;
	public final String email;
	public final String country;
	public final String postalAddress;
	
	Affiliation(String name, String email, String country, String postalAddress) {
		this.name = name;
		this.email = email;
		this.country = country;
		this.postalAddress = postalAddress;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Affiliation that = (Affiliation) o;
		return Objects.equals(name, that.name)
				&& Objects.equals(email, that.email)
				&& Objects.equals(country, that.country)
				&& Objects.equals(postalAddress, that.postalAddress);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, email, country, postalAddress);
	}

	@Override
	public String toString() {
		return "Affiliation{" +
				"name='" + name + '\'' +
				", email='" + email + '\'' +
				", country='" + country + '\'' +
				", postalAddress='" + postalAddress + '\'' +
				'}';
	}
}
