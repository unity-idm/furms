/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class PolicyDocumentService {
	private final PolicyDocumentRepository policyDocumentRepository;

	PolicyDocumentService(PolicyDocumentRepository policyDocumentRepository) {
		this.policyDocumentRepository = policyDocumentRepository;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "id")
	public Optional<Site> findById(String id) {
		LOG.debug("Getting Site with id={}", id);
		return siteRepository.findById(id);
	}
}
