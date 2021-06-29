/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.policy_documents;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.core.config.security.method.FurmsAuthorize;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.domain.authz.roles.Capability.SITE_READ;
import static io.imunity.furms.domain.authz.roles.Capability.SITE_WRITE;
import static io.imunity.furms.domain.authz.roles.ResourceType.SITE;

@Service
class PolicyDocumentServiceImpl implements PolicyDocumentService {
	private static final Logger LOG = LoggerFactory.getLogger(PolicyDocumentServiceImpl.class);

	private final PolicyDocumentRepository policyDocumentRepository;

	PolicyDocumentServiceImpl(PolicyDocumentRepository policyDocumentRepository) {
		this.policyDocumentRepository = policyDocumentRepository;
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Optional<PolicyDocument> findById(String siteId, PolicyId id) {
		LOG.debug("Getting Policy Document with id={}", id);
		return policyDocumentRepository.findById(id);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public Set<PolicyDocument> findAllBySiteId(String siteId) {
		LOG.debug("Getting all Policy Document for site id={}", siteId);
		return policyDocumentRepository.findAllBySiteId(siteId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void create(PolicyDocument policyDocument) {
		LOG.debug("Creating Policy Document for site id={}", policyDocument.siteId);
		policyDocumentRepository.create(policyDocument);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void update(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		policyDocumentRepository.update(policyDocument, false);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "policyDocument.siteId")
	public void updateWithRevision(PolicyDocument policyDocument) {
		LOG.debug("Updating Policy Document for site id={}", policyDocument.siteId);
		policyDocumentRepository.update(policyDocument, true);
	}

	@Override
	@FurmsAuthorize(capability = SITE_WRITE, resourceType = SITE, id = "siteId")
	public void delete(String siteId, PolicyId policyId) {
		LOG.debug("Deleting Policy Document {} for site id={}", policyId.id, siteId);
		policyDocumentRepository.deleteById(policyId);
	}

	@Override
	@FurmsAuthorize(capability = SITE_READ, resourceType = SITE, id = "siteId")
	public boolean isNamePresent(String siteId, String name) {
		LOG.debug("Policy Document name checking for site id={}", siteId);
		return policyDocumentRepository.isNamePresent(siteId, name);
	}
}
