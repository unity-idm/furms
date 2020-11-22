/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites;

import io.imunity.furms.api.sites.SiteServiceAPI;
import io.imunity.furms.api.sites.web.SiteResponseDTO;
import io.imunity.furms.core.sites.request.SiteDTOConverter;
import io.imunity.furms.core.sites.request.SiteDTOValidator;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.api.sites.web.SiteRequestDTO;
import io.imunity.furms.spi.sites.SiteRepository;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class SitesServiceAPI implements SiteServiceAPI {

    private final SiteRepository<Site, Long> siteRepository;
    private final SiteDTOValidator validator;
    private final SiteDTOConverter converter;

    public SitesServiceAPI(SiteRepository siteRepository,
                           SiteDTOValidator validator,
                           SiteDTOConverter converter) {
        this.siteRepository = siteRepository;
        this.validator = validator;
        this.converter = converter;
    }

    @Override
    public Optional<SiteResponseDTO> findOneById(Long id) {
        final Optional<Site> site = siteRepository.findOneById(id);
        return converter.toResponse(site);
    }

    @Override
    public Set<SiteResponseDTO> findAll() {
        return siteRepository.finAll().stream()
                .map(converter::toResponse)
                .collect(toSet());
    }

    public void create(SiteRequestDTO siteRequestDTO) {
        validator.validateCreate(siteRequestDTO);

        converter.toSite(siteRequestDTO)
                .ifPresent(siteRepository::save);
    }

    public void update(Long siteId, SiteRequestDTO siteRequestDTO) {
        validator.validateUpdate(siteId, siteRequestDTO);

        converter.toSite(siteRequestDTO)
                .ifPresent(siteRepository::save);
    }

    public void delete(Long siteId) {
        validator.validateDelete(siteId);

        siteRepository.delete(siteId);
    }
}
