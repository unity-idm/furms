/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.sites.request;

import io.imunity.furms.api.sites.web.SiteRequestDTO;
import io.imunity.furms.api.sites.web.SiteResponseDTO;
import io.imunity.furms.domain.sites.Site;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class SiteDTOConverterTest {

    private final SiteDTOConverter converter = new SiteDTOConverter();

    @Test
    void shouldConvertRequestToSite() {
        //given
        final SiteRequestDTO request = new SiteRequestDTO("name");

        //when
        final Optional<Site> site = converter.toSite(request);

        //then
        assertThat(site).isPresent();
        assertThat(site.get().getName()).isEqualTo("name");
    }

    @Test
    void shouldReturnEmptyOptionalForNullRequest() {
        //when
        final Optional<Site> site = converter.toSite(null);

        //then
        assertThat(site).isEmpty();
    }

    @Test
    void shouldConvertSiteToResponseDTO() {
        //given
        final Site site = new Site(1L, "name");

        //when
        final SiteResponseDTO response = converter.toResponse(site);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("name");
    }

    @Test
    void shouldReturnNullForNullSite() {
        //when
        final SiteResponseDTO response = converter.toResponse((Site)null);

        //then
        assertThat(response).isNull();
    }

    @Test
    void shouldConvertOptionalSiteToOptionalResponseDTO() {
        //given
        final Optional<Site> site = of(new Site(1L, "name"));

        //when
        final Optional<SiteResponseDTO> response = converter.toResponse(site);

        //then
        assertThat(response).isPresent();
        assertThat(response.get().getId()).isEqualTo(1L);
        assertThat(response.get().getName()).isEqualTo("name");
    }

    @Test
    void shouldConvertOptionalEmptySiteToOptionalEmptyResponseDTO() {
        //given
        final Optional<Site> site = empty();

        //when
        final Optional<SiteResponseDTO> response = converter.toResponse(site);

        //then
        assertThat(response).isEmpty();
    }

}