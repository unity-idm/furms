package io.imunity.furms.spi.sites;

public interface SiteRepository<T, ID> {

    T save(T site);

    boolean exists(ID siteId);

    boolean isUniqueName(String name);

    void delete(ID siteId);
}
