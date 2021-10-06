/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.communites;

import java.util.Map;

import io.imunity.furms.api.validation.exceptions.CommunityAllocationAmountNotEnoughException;
import io.imunity.furms.api.validation.exceptions.CommunityAllocationUpdateBelowDistributedAmountException;

public class CommunityAllocationErrors
{
	public static final Map<Class<? extends Exception>, String> KNOWN_ERRORS = 
			Map.of(CommunityAllocationUpdateBelowDistributedAmountException.class, 
					"view.fenix-admin.resource-credits-allocation.form.error.updateBelowAlreadyDistributed",
					CommunityAllocationAmountNotEnoughException.class,
					"view.fenix-admin.resource-credits-allocation.form.error.amountNotEnough");
}
