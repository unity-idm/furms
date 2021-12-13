/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * Checks whether particular Resource Type is used by already distributed
 * credit.
 */
@Component
class ResourceTypeDistributionChecker {

	private final ResourceCreditService resourceCreditService;

	ResourceTypeDistributionChecker(ResourceCreditService resourceCreditService) {
		this.resourceCreditService = resourceCreditService;
	}

	boolean isDistributed(ResourceTypeViewModel resourceType) {
		String resourceTypeId = resourceType.getId();
		return resourceCreditService.findAllWithAllocations(resourceType.getSiteId()).stream()
			.filter(credit -> credit.getResourceType().id.equals(resourceTypeId))
			.anyMatch(distributed());
	}

	private Predicate<? super ResourceCreditWithAllocations> distributed() {
		return creditWithAlloc -> {
			BigDecimal distributed = creditWithAlloc.getAmount().subtract(creditWithAlloc.getRemaining());
			return distributed.compareTo(BigDecimal.ZERO) != 0;
		};
	}
}
