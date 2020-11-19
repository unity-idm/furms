package io.imunity.furms.spi.sites.web;

import io.imunity.furms.domain.sites.Site;
import org.springframework.stereotype.Component;

@Component
public class SiteRequestConverter {

    public Site toSite(SiteRequest request) {
        if (request == null) {
            return null;
        }

        return Site.builder()
                .name(request.getName())
                .build();
    }

}
