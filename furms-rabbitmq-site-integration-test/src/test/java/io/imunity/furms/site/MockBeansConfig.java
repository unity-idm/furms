/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site;

import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.status_updater.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MockBeansConfig {
	@MockBean
	private ProjectInstallationStatusUpdater projectInstallationService;
	@MockBean
	private ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;
	@MockBean
	private SiteExternalIdsResolver siteExternalIdsResolver;
	@MockBean
	private SSHKeyOperationStatusUpdater sshKeyOperationService;
	@MockBean
	private UserOperationStatusUpdater userOperationStatusUpdater;
	@MockBean
	private UserAllocationStatusUpdater userAllocationStatusUpdater;
}
