/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.imunity.furms.domain.sites.Site;

class SiteComboBoxModelResolver {
	private final Map<String, SiteComboBoxModel> map;

	SiteComboBoxModelResolver(Set<Site> sites) {
		map = sites.stream().map(site -> SiteComboBoxModel.builder().id(site.getId()).name(site.getName())
				.sshKeyFromOptionMandatory(site.isSshKeyFromOptionMandatory() == null ? false
						: site.isSshKeyFromOptionMandatory())
				.build()).collect(toMap(siteModel -> siteModel.id, siteModel -> siteModel));
	}

	List<SiteComboBoxModel> getSitess() {
		return new ArrayList<>(map.values());
	}

	String getName(String id) {
		return map.get(id).name;
	}

	SiteComboBoxModel getSite(String id) {
		return map.get(id);
	}
}
