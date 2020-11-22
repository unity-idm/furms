/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.sites;

public class SiteBuilder {

    private Long id;
    private String name;

    public SiteBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public SiteBuilder name(String name) {
        this.name = name;
        return this;
    }

    public Site build() {
        return new Site(id, name);
    }

}
