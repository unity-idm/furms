/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.db;

import io.imunity.furms.db.audit_log.AuditLogEntityRepository;
import io.imunity.furms.db.resource_access.UserGrantEntityRepository;
import io.imunity.furms.db.site_agent_pending_message.SiteAgentPendingMessageEntityRepository;
import io.imunity.furms.db.user_operation.UserAdditionEntityRepository;
import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RepoCleaner {
	
	@Autowired
	private CommunityRepository communityRepository;
	@Autowired
	private ProjectRepository projectRepository;
	@Autowired
	private SiteRepository siteRepository;
	@Autowired
	private ResourceCreditRepository resourceCreditRepository;
	@Autowired
	private CommunityAllocationRepository communityAllocationRepository;
	@Autowired
	private ProjectAllocationRepository projectAllocationRepository;
	@Autowired
	private ProjectOperationRepository projectOperationRepository;
	@Autowired
	private UserAdditionEntityRepository userAdditionEntityRepository;
	@Autowired
	private UserGrantEntityRepository userGrantEntityRepository;
	@Autowired
	private PolicyDocumentRepository policyDocumentRepository;
	@Autowired
	private InfraServiceRepository infraServiceRepository;
	@Autowired
	private SiteAgentPendingMessageEntityRepository siteAgentPendingMessageEntityRepository;
	@Autowired
	private AuditLogEntityRepository auditLogRepository;

	public void cleanAll() {
		siteAgentPendingMessageEntityRepository.deleteAll();
		userGrantEntityRepository.deleteAll();
		userAdditionEntityRepository.deleteAll();
		projectOperationRepository.deleteAll();
		projectAllocationRepository.deleteAll();
		communityAllocationRepository.deleteAll();
		resourceCreditRepository.deleteAll();
		projectRepository.deleteAll();
		communityRepository.deleteAll();
		infraServiceRepository.deleteAll();
		siteRepository.deleteAll();
		policyDocumentRepository.deleteAll();
		auditLogRepository.deleteAll();
	}
}
