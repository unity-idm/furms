/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;


import io.imunity.furms.domain.ssh_key.SSHKey;

import java.time.ZoneId;
import java.util.Collections;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

class SSHKeyViewModelMapper {

	static SSHKeyViewModel map(SSHKey key, ZoneId zoneId) {

		return SSHKeyViewModel.builder().id(key.id).ownerId(key.ownerId).name(key.name)
				.sites(key.sites.isEmpty() ? Collections.emptySet() : key.sites)
				.value(key.value)
				.createTime(convertToZoneTime(key.createTime, zoneId))
				.updateTime(convertToZoneTime(key.updateTime, zoneId))
				.build();

	}

	static SSHKey map(SSHKeyViewModel sshKeyViewModel) {
		return SSHKey.builder().id(sshKeyViewModel.id).createTime(convertToUTCTime(sshKeyViewModel.createTime))
				.name(sshKeyViewModel.getName())
				.updateTime(convertToUTCTime(sshKeyViewModel.getUpdateTime()))
				.value(sshKeyViewModel.getValue()).sites(sshKeyViewModel.getSites())
				.ownerId(sshKeyViewModel.ownerId).build();

	}

}
