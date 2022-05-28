/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests.selenium;

import io.imunity.furms.end_to_end.tests.FurmsSeleniumExtension;
import io.imunity.furms.end_to_end.tests.FurmsUIChromeDriver;
import io.imunity.furms.end_to_end.tests.FurmsUIChromeDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({FurmsSeleniumExtension.class})
class ViewTypeSelectorTest {

    private FurmsUIChromeDriver.LandingPage driver;

    @BeforeEach
    void setUp() {
        driver = FurmsUIChromeDriverFactory.create()
            .getLogInPage()
            .waitSeconds(5)
            .writeUsername("a")
            .writePassword("a")
            .logIn()
            .waitSeconds(5);
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void shouldLoadFenixAdminViewType() {
        driver.getSiteView()
            .getFenixView()
            .verifyFenixView();
    }

    @Test
    void shouldLoadSiteAdminViewType() {
        driver.getFenixView()
            .getSiteView()
            .verifySiteView();
    }

    @Test
    void shouldLoadCommunityAdminViewType() {
        driver.getProjectView()
            .getCommunityView()
            .verifyCommunityView();
    }

    @Test
    void shouldLoadProjectAdminViewType() {
        driver.getCommunityView()
            .getProjectView()
            .verifyProjectView();
    }

    @Test
    void shouldLoadUserSettingsViewType() {
        driver.getFenixView()
            .getUserView()
            .verifyUserView();
    }
}
