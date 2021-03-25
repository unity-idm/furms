/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import java.math.BigDecimal;
import java.util.Objects;

class ResourceCreditComboBoxModel {
	public final String id;
	public final String name;
	public final BigDecimal amount;
	public final boolean split;

	ResourceCreditComboBoxModel(String id, String name, BigDecimal amount, boolean split) {
		this.id = id;
		this.name = name;
		this.amount = amount;
		this.split = split;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceCreditComboBoxModel that = (ResourceCreditComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceTypeComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", amount='" + amount + '\'' +
			'}';
	}
}
