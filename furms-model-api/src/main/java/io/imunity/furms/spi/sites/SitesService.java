package io.imunity.furms.spi.sites;

import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.spi.sites.web.SiteRequestConverter;
import io.imunity.furms.spi.sites.web.SiteRequest;
import io.imunity.furms.spi.sites.web.SiteRequestValidator;
import org.springframework.stereotype.Service;

import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

@Service
public class SitesService {

    private final SiteRepository<Site, Long> siteRepository;
    private final SiteRequestValidator siteRequestValidator;
    private final SiteRequestConverter siteRequestConverter;

    public SitesService(SiteRepository siteRepository,
                        SiteRequestValidator siteRequestValidator,
                        SiteRequestConverter siteRequestConverter) {
        this.siteRepository = siteRepository;
        this.siteRequestValidator = siteRequestValidator;
        this.siteRequestConverter = siteRequestConverter;
    }

    public void create(SiteRequest siteRequest) {
        siteRequestValidator.validate(siteRequest);

        final Site site = siteRequestConverter.toSite(siteRequest);
        siteRepository.save(site);
    }

    public void update(Long siteId, SiteRequest siteRequest) {
        notNull(siteId, "Site ID has to be declared.");
        state(siteRepository.exists(siteId), "Site with declared ID is not exists.");

        siteRequestValidator.validate(siteRequest);
        state(siteRepository.isUniqueName(siteRequest.getName()), "Site name is already in use.");

        final Site site = siteRequestConverter.toSite(siteRequest);
        siteRepository.save(site);
    }

    public void delete(Long siteId) {
        notNull(siteId, "Site ID has to be declared.");
        state(siteRepository.exists(siteId), "Site with declared ID is not exists.");

        siteRepository.delete(siteId);
    }
}
