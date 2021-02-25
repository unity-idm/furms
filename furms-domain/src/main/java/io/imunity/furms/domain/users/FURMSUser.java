/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.users;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FURMSUser {
	public final String id;
	public final String firstName;
	public final String lastName;
	public final String email;
	public Map<ResourceId, Set<Role>> roles;

	public FURMSUser(String id, String firstName, String lastName, String email, Map<ResourceId, Set<Role>> roles) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.roles = roles;
	}

	public FURMSUser(FURMSUser furmsUser) {
		this(furmsUser.id, furmsUser.firstName, furmsUser.lastName, furmsUser.email, null);
		this.roles = copyRoles(furmsUser.roles);
	}

	private Map<ResourceId, Set<Role>> copyRoles(Map<ResourceId, Set<Role>> roles) {
		Map<ResourceId, Set<Role>> newRoles = new HashMap<>(roles.size());
		roles.forEach((key, value) -> newRoles.put(key, Set.copyOf(value)));
		return Map.copyOf(newRoles);
	}

	public void updateRoles(Map<ResourceId, Set<Role>> roles) {
		this.roles = roles;
	}

	public static FURMSUserBuilder builder() {
		return new FURMSUserBuilder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FURMSUser furmsUser = (FURMSUser) o;
		return Objects.equals(id, furmsUser.id) &&
			Objects.equals(firstName, furmsUser.firstName) &&
			Objects.equals(lastName, furmsUser.lastName) &&
			Objects.equals(email, furmsUser.email) &&
			Objects.equals(roles, furmsUser.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, firstName, lastName, email, roles);
	}

	@Override
	public String toString() {
		return "FURMSUser{" +
			"id='" + id + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			", roles=" + roles +
			'}';
	}

	public static final class FURMSUserBuilder {
		public String id;
		public String firstName;
		public String lastName;
		public String email;
		public Map<ResourceId, Set<Role>> roles;

		private FURMSUserBuilder() {
		}

		public FURMSUserBuilder id(String id) {
			this.id = id;
			return this;
		}

		public FURMSUserBuilder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public FURMSUserBuilder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public FURMSUserBuilder email(String email) {
			this.email = email;
			return this;
		}

		public FURMSUserBuilder roles(Map<ResourceId, Set<Role>> roles) {
			this.roles = roles;
			return this;
		}

		public FURMSUser build() {
			return new FURMSUser(id, firstName, lastName, email, roles);
		}
	}
}
