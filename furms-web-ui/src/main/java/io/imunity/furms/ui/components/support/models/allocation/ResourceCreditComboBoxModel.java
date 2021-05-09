/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.support.models.allocation;

import java.math.BigDecimal;
import java.util.Objects;

public class ResourceCreditComboBoxModel {
	public final String id;
	public final String name;
	public final BigDecimal amount;
	public final boolean split;

	public ResourceCreditComboBoxModel(String id, String name, BigDecimal amount, boolean split) {
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