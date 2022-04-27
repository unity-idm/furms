/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

class SiteComboBoxModel {
	
	public final SiteId id;
	public final String name;
	public final boolean sshKeyFromOptionMandatory;

	SiteComboBoxModel(SiteId id, String name, Boolean sshKeyFromOptionMandatory) {
		this.id = id;
		this.name = name;
		this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory != null && sshKeyFromOptionMandatory;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		SiteComboBoxModel that = (SiteComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "SiteComboBoxModel{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private SiteId id;
		private String name;
		private boolean sshKeyFromOptionMandatory;

		private Builder() {
		}

		public Builder id(SiteId id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder sshKeyFromOptionMandatory(boolean sshKeyFromOptionMandatory) {
			this.sshKeyFromOptionMandatory = sshKeyFromOptionMandatory;
			return this;
		}

		public SiteComboBoxModel build() {
			return new SiteComboBoxModel(id, name, sshKeyFromOptionMandatory);
		}
	}

	
	
}
