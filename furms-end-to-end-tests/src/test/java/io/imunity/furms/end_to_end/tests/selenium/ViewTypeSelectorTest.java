/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests.selenium;

import io.imunity.furms.end_to_end.tests.FurmsSeleniumExtension;
import io.imunity.furms.end_to_end.tests.FurmsUIDriver;
import io.imunity.furms.end_to_end.tests.FurmsUIChromeDriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith({FurmsSeleniumExtension.class})
class ViewTypeSelectorTest {

    private FurmsUIDriver.LandingPage driver;

    @BeforeEach
    void setUp() {
        driver = FurmsUIChromeDriverFactory.create()
            .getLogInPage()
            .assertViewIsVisible()
            .writeUsername("a")
            .writePassword("a")
            .logIn()
            .assertViewIsVisible();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void shouldLoadFenixAdminViewType() {
        driver.visitSiteView()
            .visitFenixView()
            .assertFenixViewIsVisible();
    }

    @Test
    void shouldLoadSiteAdminViewType() {
        driver.visitFenixView()
            .visitSiteView()
            .assertSiteViewIsVisible();
    }

    @Test
    void shouldLoadCommunityAdminViewType() {
        driver.visitProjectView()
            .visitCommunityView()
            .assertCommunityViewIsVisible();
    }

    @Test
    void shouldLoadProjectAdminViewType() {
        driver.visitCommunityView()
            .visitProjectView()
            .assertProjectViewIsVisible();
    }

    @Test
    void shouldLoadUserSettingsViewType() {
        driver.visitFenixView()
            .visitUserView()
            .assertUserViewIsVisible();
    }
}
