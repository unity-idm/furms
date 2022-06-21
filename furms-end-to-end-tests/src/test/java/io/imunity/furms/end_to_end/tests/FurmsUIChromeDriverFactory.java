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
		String chromeDriverPath = System.getenv("CHROME_DRIVER_PATH");
		if(chromeDriverPath == null)
			throw new RuntimeException("System env CHROME_DRIVER_PATH have to be set!");
		System.setProperty("webdriver.chrome.driver", chromeDriverPath);

		ChromeOptions options = new ChromeOptions();
		options.addArguments("window-size=1280,1024", "no-sandbox", "force-device-scale-factor=1",
			"ignore-certificate-errors", "allow-insecure-localhost");
		String seleniumOpts = System.getProperty("selenium.opts");
		if (seleniumOpts != null && !seleniumOpts.isEmpty())
		{
			String[] opts = seleniumOpts.split(",");
			options.addArguments(opts);
		}
		WebDriver driver = new ChromeDriver(options);

		return new FurmsUIDriver<>(driver);
	}
}
