/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
class SiteServiceMock implements SiteService {

    @Override
    @PreAuthorize("isMember()")
    public Optional<Site> findById(String id) {
        return Optional.empty();
    }

    @Override
    @PreAuthorize("isMember()")
    public Set<Site> findAll() {
        return null;
    }

    @Override
    public void create(Site site) {

    }

    @Override
    public void update(Site site) {

    }

    @Override
    public void delete(String id) {

    }
}
