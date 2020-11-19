package io.imunity.furms.spi.sites.web;

import org.springframework.stereotype.Component;

import static org.springframework.util.Assert.notNull;

@Component
public class SiteRequestValidator {

    public void validate(SiteRequest request) {
        notNull(request, "Site object cannot be null.");
        validateName(request);
    }

    private void validateName(SiteRequest request) {
        notNull(request.getName(), "Site name has to be declared.");
    }

}
