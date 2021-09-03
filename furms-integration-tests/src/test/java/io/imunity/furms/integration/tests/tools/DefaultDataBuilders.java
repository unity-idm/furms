/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests.tools;

import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_installation.ProjectInstallationJob;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.domain.resource_usage.ResourceUsage;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DefaultDataBuilders {

	public static Site.SiteBuilder defaultSite() {
		return Site.builder()
			.id(UUID.randomUUID().toString())
			.name("Default Site Name")
			.oauthClientId("default_oauth_client")
			.connectionInfo("Default Connection Info")
			.logo(new FurmsImage("image".getBytes(), "jpg"))
			.sshKeyFromOptionMandatory(false)
			.externalId(new SiteExternalId("se_id"))
			.sshKeyHistoryLength(1)
			.policyId(PolicyId.empty());
	}

	public static Community.CommunityBuilder defaultCommunity() {
		return Community.builder()
				.id(UUID.randomUUID().toString())
				.name("Default Community Name")
				.description("Default Community Description")
				.logo("logo".getBytes(), "jpg");
	}

	public static CommunityAllocation.CommunityAllocationBuilder defaultCommunityAllocation() {
		return CommunityAllocation.builder()
				.id(UUID.randomUUID().toString())
				.communityId(UUID.randomUUID().toString())
				.resourceCreditId(UUID.randomUUID().toString())
				.name("Default Community Name")
				.amount(BigDecimal.valueOf(100));
	}

	public static Project.ProjectBuilder defaultProject() {
		return Project.builder()
				.id(UUID.randomUUID().toString())
				.communityId(UUID.randomUUID().toString())
				.name("Default Project Name")
				.description("Default Project Description")
				.logo(new FurmsImage("logo".getBytes(), "jpg"))
				.acronym("DPN")
				.researchField("DPN")
				.utcStartTime(LocalDateTime.now().minusMinutes(10l))
				.utcEndTime(LocalDateTime.now().plusMinutes(10l))
				.leaderId(new PersistentId(UUID.randomUUID().toString()));
	}

	public static ProjectAllocation.ProjectAllocationBuilder defaultProjectAllocation() {
		return ProjectAllocation.builder()
				.id(UUID.randomUUID().toString())
				.projectId(UUID.randomUUID().toString())
				.communityAllocationId(UUID.randomUUID().toString())
				.name("Default Project Allocation Name")
				.amount(BigDecimal.valueOf(100));
	}

	public static ProjectAllocationChunk.ProjectAllocationChunkBuilder defaultProjectAllocationChunk() {
		return ProjectAllocationChunk.builder()
				.id(UUID.randomUUID().toString())
				.projectAllocationId(UUID.randomUUID().toString())
				.chunkId(UUID.randomUUID().toString())
				.amount(BigDecimal.ONE)
				.validFrom(LocalDateTime.now().minusMinutes(1))
				.validTo(LocalDateTime.now().plusMinutes(1))
				.receivedTime(LocalDateTime.now().minusMinutes(2));
	}

	public static ResourceUsage.ResourceUsageBuilder defaultResourceUsage() {
		return ResourceUsage.builder()
				.projectId(UUID.randomUUID().toString())
				.projectAllocationId(UUID.randomUUID().toString())
				.cumulativeConsumption(BigDecimal.valueOf(0.1))
				.probedAt(LocalDateTime.now().minusMinutes(1));
	}

	public static UserResourceUsage.UserResourceUsageBuilder defaultUserResourceUsage() {
		return UserResourceUsage.builder()
				.projectId(UUID.randomUUID().toString())
				.projectAllocationId(UUID.randomUUID().toString())
				.fenixUserId(new FenixUserId(UUID.randomUUID().toString()))
				.cumulativeConsumption(BigDecimal.valueOf(0.1))
				.consumedUntil(LocalDateTime.now().minusMinutes(1));
	}

	public static ProjectInstallationJob.ProjectInstallationJobBuilder defaultProjectInstallationJob() {
		return ProjectInstallationJob.builder()
				.id(UUID.randomUUID().toString())
				.projectId(UUID.randomUUID().toString())
				.siteId(UUID.randomUUID().toString())
				.correlationId(new CorrelationId(UUID.randomUUID().toString()))
				.gid(UUID.randomUUID().toString())
				.status(ProjectInstallationStatus.INSTALLED);
	}

	public static ResourceCredit.ResourceCreditBuilder defaultResourceCredit() {
		return ResourceCredit.builder()
				.id(UUID.randomUUID().toString())
				.name("Default Resource Credit Name")
				.siteId(UUID.randomUUID().toString())
				.resourceTypeId(UUID.randomUUID().toString())
				.splittable(false)
				.amount(BigDecimal.TEN)
				.utcCreateTime(LocalDateTime.now().minusMinutes(10l))
				.utcStartTime(LocalDateTime.now().minusMinutes(10l))
				.utcEndTime(LocalDateTime.now().plusMinutes(10l));
	}

	public static ResourceType.ResourceTypeBuilder defaultResourceType() {
		return ResourceType.builder()
				.id(UUID.randomUUID().toString())
				.name("Default Resource Type Name")
				.siteId(UUID.randomUUID().toString())
				.serviceId(UUID.randomUUID().toString())
				.type(ResourceMeasureType.INTEGER)
				.unit(ResourceMeasureUnit.GB)
				.accessibleForAllProjectMembers(false);
	}

	public static UserAddition.UserAdditionBuilder defaultUserAddition() {
		return UserAddition.builder()
				.id(UUID.randomUUID().toString())
				.siteId(new SiteId(UUID.randomUUID().toString()))
				.projectId(UUID.randomUUID().toString())
				.correlationId(new CorrelationId(UUID.randomUUID().toString()))
				.uid(UUID.randomUUID().toString())
				.userId(UUID.randomUUID().toString())
				.status(UserStatus.ADDED)
				.errorMessage(null);
	}

	public static InfraService.ServiceBuilder defaultService() {
		return InfraService.builder()
				.id(UUID.randomUUID().toString())
				.name("Default Service Name")
				.description("Default Service Description")
				.siteId(UUID.randomUUID().toString())
				.policyId(new PolicyId(UUID.randomUUID()));
	}

	public static PolicyDocument.PolicyDocumentBuilder defaultPolicy() {
		return PolicyDocument.builder()
				.id(new PolicyId(UUID.randomUUID()))
				.siteId(UUID.randomUUID().toString())
				.name("Default Policy")
				.workflow(PolicyWorkflow.WEB_BASED)
				.revision(1)
				.contentType(PolicyContentType.EMBEDDED)
				.wysiwygText("Default HTML Policy Text");
	}

}
