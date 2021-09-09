/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.integration.tests;

import io.imunity.furms.spi.communites.CommunityRepository;
import io.imunity.furms.spi.community_allocation.CommunityAllocationRepository;
import io.imunity.furms.spi.policy_docuemnts.PolicyDocumentRepository;
import io.imunity.furms.spi.project_allocation.ProjectAllocationRepository;
import io.imunity.furms.spi.project_allocation_installation.ProjectAllocationInstallationRepository;
import io.imunity.furms.spi.project_installation.ProjectOperationRepository;
import io.imunity.furms.spi.projects.ProjectRepository;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.resource_credits.ResourceCreditRepository;
import io.imunity.furms.spi.resource_type.ResourceTypeRepository;
import io.imunity.furms.spi.resource_usage.ResourceUsageRepository;
import io.imunity.furms.spi.services.InfraServiceRepository;
import io.imunity.furms.spi.sites.SiteRepository;
import io.imunity.furms.spi.ssh_key_history.SSHKeyHistoryRepository;
import io.imunity.furms.spi.ssh_key_installation.InstalledSSHKeyRepository;
import io.imunity.furms.spi.ssh_key_operation.SSHKeyOperationRepository;
import io.imunity.furms.spi.ssh_keys.SSHKeyRepository;
import io.imunity.furms.spi.tokens.AccessTokenRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.api.key.UserApiKeyRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class DBTestManager {

	@Autowired protected CommunityRepository communityRepository;
	@Autowired protected CommunityAllocationRepository communityAllocationRepository;
	@Autowired protected PolicyDocumentRepository policyDocumentRepository;
	@Autowired protected ProjectAllocationRepository projectAllocationRepository;
	@Autowired protected ProjectAllocationInstallationRepository projectAllocationInstallationRepository;
	@Autowired protected ProjectOperationRepository projectOperationRepository;
	@Autowired protected ProjectRepository projectRepository;
	@Autowired protected ResourceAccessRepository resourceAccessRepository;
	@Autowired protected ResourceCreditRepository resourceCreditRepository;
	@Autowired protected ResourceTypeRepository resourceTypeRepository;
	@Autowired protected ResourceUsageRepository resourceUsageRepository;
	@Autowired protected InfraServiceRepository infraServiceRepository;
	@Autowired protected SiteRepository siteRepository;
	@Autowired protected SSHKeyHistoryRepository sshKeyHistoryRepository;
	@Autowired protected InstalledSSHKeyRepository installedSSHKeyRepository;
	@Autowired protected SSHKeyOperationRepository sshKeyOperationRepository;
	@Autowired protected SSHKeyRepository sshKeyRepository;
	@Autowired protected AccessTokenRepository accessTokenRepository;
	@Autowired protected UserOperationRepository userOperationRepository;
	@Autowired protected UserApiKeyRepository userApiKeyRepository;
	@Autowired protected ResourceAccessRepository resourceAccessDatabaseRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@AfterEach
	void tearDown() {
		jdbcTemplate.execute("DELETE FROM installed_ssh_key");
		jdbcTemplate.execute("DELETE FROM project");
		jdbcTemplate.execute("DELETE FROM project_allocation");
		jdbcTemplate.execute("DELETE FROM project_allocation_chunk");
		jdbcTemplate.execute("DELETE FROM project_allocation_installation");
		jdbcTemplate.execute("DELETE FROM project_deallocation");
		jdbcTemplate.execute("DELETE FROM project_installation_job");
		jdbcTemplate.execute("DELETE FROM project_update_job");
		jdbcTemplate.execute("DELETE FROM community_allocation");
		jdbcTemplate.execute("DELETE FROM community");
		jdbcTemplate.execute("DELETE FROM resource_credit");
		jdbcTemplate.execute("DELETE FROM resource_type");
		jdbcTemplate.execute("DELETE FROM resource_usage");
		jdbcTemplate.execute("DELETE FROM resource_usage_history");
		jdbcTemplate.execute("DELETE FROM service");
		jdbcTemplate.execute("DELETE FROM site");
		jdbcTemplate.execute("DELETE FROM policy_document");
		jdbcTemplate.execute("DELETE FROM ssh_key_history");
		jdbcTemplate.execute("DELETE FROM ssh_key_operation_job");
		jdbcTemplate.execute("DELETE FROM sshkey");
		jdbcTemplate.execute("DELETE FROM sshkey_site");
		jdbcTemplate.execute("DELETE FROM user_addition");
		jdbcTemplate.execute("DELETE FROM user_addition_job");
		jdbcTemplate.execute("DELETE FROM user_api_key");
		jdbcTemplate.execute("DELETE FROM user_grant");
		jdbcTemplate.execute("DELETE FROM user_grant_job");
		jdbcTemplate.execute("DELETE FROM user_resource_usage");
		jdbcTemplate.execute("DELETE FROM user_resource_usage_history");
	}
}
