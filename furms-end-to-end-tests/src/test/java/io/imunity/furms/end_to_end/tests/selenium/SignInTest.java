/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests.selenium;

import io.imunity.furms.end_to_end.tests.FurmsSeleniumExtension;
import io.imunity.furms.end_to_end.tests.FurmsUIChromeDriverFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({FurmsSeleniumExtension.class})
class SignInTest {
    private static final int LANDING_PAGE_ROLES_NUMBER = 5;

    @Test
    void shouldSignIn() {
        FurmsUIChromeDriverFactory.create()
            .getLogInPage()
            .assertViewIsVisible()
            .writeUsername("a")
            .writePassword("a")
            .logIn()
            .assertViewIsVisible()
            .verifyLandingPageRolesNumber(LANDING_PAGE_ROLES_NUMBER)
            .quit();
    }
}
