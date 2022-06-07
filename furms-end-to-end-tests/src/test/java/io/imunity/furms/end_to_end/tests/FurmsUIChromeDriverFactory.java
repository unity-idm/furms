/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class FurmsUIChromeDriverFactory {
	public static FurmsUIDriver<?> create(){
		String getenv = System.getenv("CHROME_DRIVER_PATH");
		System.setProperty("webdriver.chrome.driver", getenv);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("allow-insecure-localhost");
		WebDriver driver = new ChromeDriver(options);

		return new FurmsUIDriver<>(driver);
	}
}
