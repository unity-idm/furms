/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyId;
import io.imunity.furms.domain.ssh_keys.SSHKeyOperationJob;

import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class SSHKeyViewModelMapper {

	static SSHKeyViewModel map(SSHKey key, ZoneId zoneId,
			Function<SSHKeyId, List<SSHKeyOperationJob>> statusSupplier) {

		return SSHKeyViewModel.builder().id(key.id).ownerId(key.ownerId).name(key.name)
				.sites(getKeyStatus(key, statusSupplier)).value(key.value)
				.createTime(convertToZoneTime(key.createTime, zoneId)).build();
	}

	private static Set<SiteWithKeyStatus> getKeyStatus(SSHKey sshKey,
			Function<SSHKeyId, List<SSHKeyOperationJob>> statusSupplier) {
		Set<SiteWithKeyStatus> sitesWithKeyStatus = new HashSet<>();
		for (SSHKeyOperationJob findBySSHKeyIdAndSiteId : statusSupplier.apply(sshKey.id)) {
			if (findBySSHKeyIdAndSiteId != null) {
				sitesWithKeyStatus.add(new SiteWithKeyStatus(findBySSHKeyIdAndSiteId.siteId,
						findBySSHKeyIdAndSiteId.operation, findBySSHKeyIdAndSiteId.status,
						findBySSHKeyIdAndSiteId.error));
			}
		}

		return sitesWithKeyStatus;
	}

	static SSHKeyUpdateModel mapToUpdate(SSHKey key, ZoneId zoneId) {

		return SSHKeyUpdateModel.builder().id(key.id).ownerId(key.ownerId).name(key.name)
				.sites(key.sites.isEmpty() ? Collections.emptySet() : key.sites).value(key.value)
				.createTime(convertToZoneTime(key.createTime, zoneId))
				.updateTime(convertToZoneTime(key.updateTime, zoneId)).build();

	}

	static SSHKey map(SSHKeyUpdateModel sshKeyViewModel) {
		return SSHKey.builder().id(sshKeyViewModel.id).createTime(convertToUTCTime(sshKeyViewModel.createTime))
				.name(sshKeyViewModel.getName())
				.updateTime(convertToUTCTime(sshKeyViewModel.getUpdateTime()))
				.value(sshKeyViewModel.getValue()).sites(sshKeyViewModel.getSites())
				.ownerId(sshKeyViewModel.ownerId).build();

	}

}
