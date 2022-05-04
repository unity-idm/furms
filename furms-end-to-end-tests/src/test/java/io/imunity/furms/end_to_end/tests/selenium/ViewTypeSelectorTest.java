/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests.selenium;

import io.imunity.furms.end_to_end.tests.FurmsSeleniumExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@ExtendWith({FurmsSeleniumExtension.class})
class ViewTypeSelectorTest {

    private static final int FENIX_ADMIN_VIEW_LANDING_PAGE_ID = 0;
    private static final int SITE_ADMIN_VIEW_LANDING_PAGE_ID = 1;

    private static final int FENIX_ADMIN_VIEW_SELECTOR_ID = 1;
    private static final int SITE_ADMIN_VIEW_SELECTOR_ID = 2;
    private static final int COMMUNITY_ADMIN_VIEW_SELECTOR_ID = 3;
    private static final int PROJECT_ADMIN_VIEW_SELECTOR_ID = 4;
    private static final int USER_SETTINGS_VIEW_SELECTOR_ID = 5;

    private WebDriver driver;
    private WebDriverWait waitDriver;

    @BeforeEach
    void setUp() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("allow-insecure-localhost");
        driver = new ChromeDriver(options);
        waitDriver = new WebDriverWait(driver, 5);
        driver.get("https://localhost:3443/?showSignInOptions");

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.findElements(By.tagName("input")).forEach(x -> x.sendKeys("a"));
        driver.findElement(By.xpath("//div[@class='v-slot v-slot-u-signInButton v-slot-u-passwordSignInButton']")).click();

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void shouldLoadingFenixAdminViewType() {
        driver.findElements(By.tagName("vaadin-button")).get(SITE_ADMIN_VIEW_LANDING_PAGE_ID).click();

        driver.findElement(By.className("furms-select")).click();

        driver.findElements(By.tagName("vaadin-select-item")).get(FENIX_ADMIN_VIEW_SELECTOR_ID).click();

        waitDriver.until(ExpectedConditions.urlContains("fenix/admin/dashboard"));
    }

    @Test
    void shouldLoadingSiteAdminViewType() {
        driver.findElements(By.tagName("vaadin-button")).get(FENIX_ADMIN_VIEW_LANDING_PAGE_ID).click();

        driver.findElement(By.className("furms-select")).click();

        driver.findElements(By.tagName("vaadin-select-item")).get(SITE_ADMIN_VIEW_SELECTOR_ID).click();

        waitDriver.until(ExpectedConditions.urlContains("site/admin/policy/document"));
    }

    @Test
    void shouldLoadingCommunityAdminViewType() {
        driver.findElements(By.tagName("vaadin-button")).get(FENIX_ADMIN_VIEW_LANDING_PAGE_ID).click();

        driver.findElement(By.className("furms-select")).click();

        driver.findElements(By.tagName("vaadin-select-item")).get(COMMUNITY_ADMIN_VIEW_SELECTOR_ID).click();

        waitDriver.until(ExpectedConditions.urlContains("community/admin/dashboard"));
    }

    @Test
    void shouldLoadingProjectAdminViewType() {
        driver.findElements(By.tagName("vaadin-button")).get(FENIX_ADMIN_VIEW_LANDING_PAGE_ID).click();

        driver.findElement(By.className("furms-select")).click();

        driver.findElements(By.tagName("vaadin-select-item")).get(PROJECT_ADMIN_VIEW_SELECTOR_ID).click();

        waitDriver.until(ExpectedConditions.urlContains("project/admin/users"));
    }

    @Test
    void shouldLoadingUserSettingsViewType() {
        driver.findElements(By.tagName("vaadin-button")).get(FENIX_ADMIN_VIEW_LANDING_PAGE_ID).click();

        driver.findElement(By.className("furms-select")).click();

        driver.findElements(By.tagName("vaadin-select-item")).get(USER_SETTINGS_VIEW_SELECTOR_ID).click();

        waitDriver.until(ExpectedConditions.urlContains("users/settings/profile"));
    }
}
