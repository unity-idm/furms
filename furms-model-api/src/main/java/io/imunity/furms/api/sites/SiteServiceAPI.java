/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites;

import io.imunity.furms.api.sites.web.SiteRequestDTO;
import io.imunity.furms.api.sites.web.SiteResponseDTO;

import java.util.Optional;
import java.util.Set;

public interface SiteServiceAPI {

    Optional<SiteResponseDTO> findOneById(Long id);

    Set<SiteResponseDTO> findAll();

    void create(SiteRequestDTO object);

    void update(Long id, SiteRequestDTO object);

    void delete(Long id);

}
