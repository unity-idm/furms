/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites.request;

import io.imunity.furms.api.sites.web.SiteRequestDTO;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.SiteRepository;
import org.springframework.stereotype.Component;

import static org.springframework.util.Assert.notNull;

@Component
public class SiteDTOValidator {

    private final SiteRepository<Site, Long> siteRepository;

    public SiteDTOValidator(SiteRepository<Site, Long> siteRepository) {
        this.siteRepository = siteRepository;
    }

    public void validateCreate(SiteRequestDTO request) {
        notNull(request, "Site object cannot be null.");
        validateName(request);
    }

    public void validateUpdate(Long siteId, SiteRequestDTO request) {
        validateId(siteId);
        notNull(request, "Site object cannot be null.");
        validateName(request);
    }

    public void validateDelete(Long siteId) {
        validateId(siteId);
    }

    private void validateName(SiteRequestDTO request) {
        notNull(request.getName(), "Site name has to be declared.");
        if (!siteRepository.isUniqueName(request.getName())) {
            throw new IllegalArgumentException("Site name has to be unique.");
        }
    }

    private void validateId(Long siteId) {
        notNull(siteId, "Site ID has to be declared.");
        if (!siteRepository.exists(siteId)) {
            throw new IllegalArgumentException("Site with declared ID is not exists.");
        }
    }

}
