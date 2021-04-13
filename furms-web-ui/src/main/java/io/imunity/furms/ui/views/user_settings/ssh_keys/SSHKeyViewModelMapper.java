/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import static io.imunity.furms.ui.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.ui.utils.UTCTimeUtils.convertToZoneTime;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.imunity.furms.domain.ssh_key.SSHKey;

class SSHKeyViewModelMapper {

	static List<SSHKeyViewModel> map(SSHKey key, ZoneId zoneId) {

		if (key.sites.isEmpty())
			return List.of(SSHKeyViewModel.builder().id(key.id).ownerId(key.ownerId).name(key.name)
					.rowSiteId(null).sites(Collections.emptySet()).value(key.value)
					.createTime(convertToZoneTime(key.createTime, zoneId))
					.updateTime(convertToZoneTime(key.updateTime, zoneId)).build());

		return key.sites.stream()
				.map(s -> SSHKeyViewModel.builder().id(key.id).ownerId(key.ownerId).name(key.name)
						.rowSiteId(s).sites(key.sites).value(key.value)
						.createTime(convertToZoneTime(key.createTime, zoneId))
						.updateTime(convertToZoneTime(key.updateTime, zoneId)).build())
				.collect(Collectors.toList());

	}

	static SSHKey map(SSHKeyViewModel sshKeyViewModel) {
		return SSHKey.builder().id(sshKeyViewModel.id).createTime(convertToUTCTime(sshKeyViewModel.createTime))
				.name(sshKeyViewModel.getName())
				.updateTime(convertToUTCTime(sshKeyViewModel.getUpdateTime()))
				.value(sshKeyViewModel.getValue()).sites(sshKeyViewModel.getSites())
				.ownerId(sshKeyViewModel.ownerId).build();

	}

}
