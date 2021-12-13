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
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.users.UserStatus.DISABLED;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.Optional.ofNullable;

public class FURMSUser {
	public final Optional<PersistentId> id;
	public final Optional<FenixUserId> fenixUserId;
	public final Optional<String> firstName;
	public final Optional<String> lastName;
	public final String email;
	public final UserStatus status;
	public final Map<ResourceId, Set<Role>> roles;

	private FURMSUser(PersistentId id, FenixUserId fenixUserId,
					  String firstName,
					  String lastName,
					  String email,
					  UserStatus status,
					  Map<ResourceId, Set<Role>> roles) {
		if (email == null)
			throw new IllegalArgumentException("Email must be not null");
		this.id = ofNullable(id);
		this.fenixUserId = ofNullable(fenixUserId);
		this.firstName = ofNullable(firstName);
		this.lastName = ofNullable(lastName);
		this.email = email;
		this.status = status == null ? DISABLED : status;
		this.roles = copyRoles(roles);
	}

	public FURMSUser(FURMSUser furmsUser) {
		this(furmsUser.id.orElse(null), furmsUser.fenixUserId.orElse(null),  furmsUser.firstName.orElse(null), furmsUser.lastName.orElse(null),
				furmsUser.email, furmsUser.status, furmsUser.roles);
	}

	public FURMSUser(FURMSUser furmsUser, Map<ResourceId, Set<Role>> roles) {
		this(furmsUser.id.orElse(null), furmsUser.fenixUserId.orElse(null), furmsUser.firstName.orElse(null), furmsUser.lastName.orElse(null),
				furmsUser.email, furmsUser.status, roles);
	}

	private static Map<ResourceId, Set<Role>> copyRoles(Map<ResourceId, Set<Role>> roles) {
		if(roles == null)
			return emptyMap();
		Map<ResourceId, Set<Role>> newRoles = new HashMap<>(roles.size());
		roles.forEach((key, value) -> newRoles.put(key, Set.copyOf(value)));
		return unmodifiableMap(newRoles);
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
			Objects.equals(fenixUserId, furmsUser.fenixUserId) &&
			Objects.equals(firstName, furmsUser.firstName) &&
			Objects.equals(lastName, furmsUser.lastName) &&
			Objects.equals(email, furmsUser.email) &&
			Objects.equals(status, furmsUser.status) &&
			Objects.equals(roles, furmsUser.roles);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, fenixUserId, firstName, lastName, email, status, roles);
	}

	@Override
	public String toString() {
		return "FURMSUser{" +
			"id='" + id + '\'' +
			"fenixUserId='" + fenixUserId + '\'' +
			", firstName='" + firstName + '\'' +
			", lastName='" + lastName + '\'' +
			", email='" + email + '\'' +
			", status='" + status + '\'' +
			", roles=" + roles +
			'}';
	}

	public static final class FURMSUserBuilder {
		public PersistentId id;
		public FenixUserId fenixUserId;
		public String firstName;
		public String lastName;
		public String email;
		public UserStatus status;
		public Map<ResourceId, Set<Role>> roles;

		private FURMSUserBuilder() {
		}

		public FURMSUserBuilder id(PersistentId id) {
			this.id = id;
			return this;
		}
		
		public FURMSUserBuilder fenixUserId(FenixUserId fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public FURMSUserBuilder fenixUserId(String fenixUserId) {
			if(fenixUserId == null || fenixUserId.isEmpty())
				return this;
			this.fenixUserId = new FenixUserId(fenixUserId);
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

		public FURMSUserBuilder status(UserStatus status) {
			this.status = status;
			return this;
		}

		public FURMSUserBuilder roles(Map<ResourceId, Set<Role>> roles) {
			this.roles = roles;
			return this;
		}

		public FURMSUser build() {
			return new FURMSUser(id, fenixUserId, firstName, lastName, email, status, roles);
		}
	}
}
