/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

import java.util.Objects;

public class Site {

    private final Long id;
    private final String name;

    public Site(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static SiteBuilder builder() {
        return new SiteBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Site site = (Site) o;
        return Objects.equals(id, site.id) &&
                Objects.equals(name, site.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Sites{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

}
