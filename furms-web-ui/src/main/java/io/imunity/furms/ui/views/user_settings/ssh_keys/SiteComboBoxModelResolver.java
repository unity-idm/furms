/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import io.imunity.furms.domain.sites.Site;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

class SiteComboBoxModelResolver {
	private final Map<String, SiteComboBoxModel> map;

	SiteComboBoxModelResolver(Set<Site> sites) {
		map = sites.stream().map(site -> SiteComboBoxModel.builder().id(site.getId()).name(site.getName())
				.sshKeyFromOptionMandatory(site.isSshKeyFromOptionMandatory() != null && site.isSshKeyFromOptionMandatory())
				.build()).collect(toMap(siteModel -> siteModel.id, siteModel -> siteModel));
	}

	List<SiteComboBoxModel> getSites() {
		return new ArrayList<>(map.values());
	}

	String getName(String id) {
		return map.get(id) != null ? map.get(id).name : id;
	}

	SiteComboBoxModel getSite(String id) {
		return map.get(id) != null ? map.get(id) : new SiteComboBoxModel(id, id, false);
	}
}
