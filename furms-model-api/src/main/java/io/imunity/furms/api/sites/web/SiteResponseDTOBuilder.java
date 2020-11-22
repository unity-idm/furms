/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites.web;

public final class SiteResponseDTOBuilder {

    private Long id;
    private String name;

    public SiteResponseDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public SiteResponseDTOBuilder name(String name) {
        this.name = name;
        return this;
    }

    public SiteResponseDTO build() {
        return new SiteResponseDTO(id, name);
    }
}
