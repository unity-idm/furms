/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests.selenium;

import io.imunity.furms.end_to_end.tests.FurmsSeleniumExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({FurmsSeleniumExtension.class})
class SignInTest {
    private static final int LANDING_PAGE_ROLES_NUMBER = 5;

    @Test
    void shouldSignIn() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("allow-insecure-localhost");
        WebDriver driver = new ChromeDriver(options);
        driver.get("https://localhost:3443/?showSignInOptions");

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.findElements(By.tagName("input")).forEach(x -> x.sendKeys("a"));
        driver.findElement(By.xpath("//div[@class='v-slot v-slot-u-signInButton v-slot-u-passwordSignInButton']")).click();

        new WebDriverWait(driver, 5).until(ExpectedConditions.urlContains("front/start/role/chooser"));
        assertThat(driver.findElements(By.tagName("vaadin-button")).size()).isEqualTo(LANDING_PAGE_ROLES_NUMBER);

        driver.quit();
    }
}
