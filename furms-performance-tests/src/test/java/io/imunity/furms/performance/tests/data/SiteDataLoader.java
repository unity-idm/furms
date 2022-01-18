/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;

import static io.imunity.furms.performance.tests.data.DataLoaderUtils.randomName;
import static java.util.stream.Collectors.toSet;

class SiteDataLoader {

	private final PolicyDocumentService policyDocumentService;
	private final ResourceTypeService resourceTypeService;
	private final InfraServiceService infraServiceService;
	private final SiteService siteService;

	SiteDataLoader(PolicyDocumentService policyDocumentService,
	               ResourceTypeService resourceTypeService,
	               InfraServiceService infraServiceService,
	               SiteService siteService) {
		this.policyDocumentService = policyDocumentService;
		this.resourceTypeService = resourceTypeService;
		this.infraServiceService = infraServiceService;
		this.siteService = siteService;
	}

	Set<Data.Site> loadSites(final long sitesCount) {
		LongStream.range(0, sitesCount)
				.forEach(i -> siteService.create(Site.builder()
						.name(randomName())
						.build()));
		System.out.println("Starting loading sites");
		return siteService.findAll().stream()
				.map(site -> {
					policyDocumentService.create(PolicyDocument.builder()
							.siteId(site.getId())
							.name(randomName())
							.workflow(PolicyWorkflow.WEB_BASED)
							.contentType(PolicyContentType.EMBEDDED)
							.wysiwygText("test")
							.revisionUndefined()
							.build());
					final PolicyDocument policy = policyDocumentService.findAllBySiteId(site.getId()).stream().findFirst().get();
					siteService.update(Site.builder()
							.id(site.getId())
							.name(site.getName())
							.externalId(site.getExternalId())
							.policyId(policy.id)
							.build());
					infraServiceService.create(InfraService.builder()
							.siteId(site.getId())
							.name(randomName())
							.description(UUID.randomUUID().toString())
							.build());
					final InfraService infraService = infraServiceService.findAll(site.getId()).stream().findFirst().get();
					resourceTypeService.create(ResourceType.builder()
							.name(randomName())
							.siteId(site.getId())
							.serviceId(infraService.id)
							.type(ResourceMeasureType.DATA)
							.unit(ResourceMeasureUnit.GB)
							.build());
					final ResourceType resourceType = resourceTypeService.findAll(site.getId()).stream().findFirst().get();
					return new Data.Site(new SiteId(site.getId()), policy.id, resourceType.id);
				})
				.collect(toSet());
	}
}
