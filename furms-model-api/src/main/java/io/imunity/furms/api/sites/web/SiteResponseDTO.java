/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites.web;

import java.util.Objects;

public class SiteResponseDTO {

    private final Long id;
    private final String name;

    public SiteResponseDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static SiteResponseDTOBuilder builder() {
        return new SiteResponseDTOBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteResponseDTO siteResponseDTO = (SiteResponseDTO) o;
        return Objects.equals(id, siteResponseDTO.id) &&
                Objects.equals(name, siteResponseDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "SiteDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}
