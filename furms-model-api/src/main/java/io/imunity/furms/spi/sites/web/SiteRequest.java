package io.imunity.furms.spi.sites.web;

import java.util.Objects;

public class SiteRequest {
    private final String name;

    public SiteRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SiteRequest that = (SiteRequest) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "CreateSiteRequest{" +
                "name='" + name + '\'' +
                '}';
    }
}
