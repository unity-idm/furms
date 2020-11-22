/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites.request;

import io.imunity.furms.api.sites.web.SiteRequestDTO;
import io.imunity.furms.api.sites.web.SiteResponseDTO;
import io.imunity.furms.domain.sites.Site;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class SiteDTOConverter {

    public Optional<Site> toSite(SiteRequestDTO request) {
        return ofNullable(request).map(object -> Site.builder()
                .name(object.getName())
                .build());
    }

    public SiteResponseDTO toResponse(Site site) {
        return toResponse(ofNullable(site))
                .orElse(null);
    }

    public Optional<SiteResponseDTO> toResponse(Optional<Site> site) {
        return site.map(object -> SiteResponseDTO.builder()
                .id(object.getId())
                .name(object.getName())
                .build());
    }

}
