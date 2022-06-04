/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.end_to_end.tests;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FurmsUIDriver<T extends FurmsUIDriver<T>> {
	private static final int TIMEOUT_IN_SECONDS = 5;

	protected final WebDriver driver;
	private final WebDriverWait waitDriver;

	FurmsUIDriver(WebDriver driver) {
		this.driver = driver;
		this.waitDriver = new WebDriverWait(driver, TIMEOUT_IN_SECONDS);
	}

	public LoginPage getLogInPage(){
		driver.get("https://localhost:3443/?showSignInOptions");
		return new LoginPage(driver);
	}

	public T waitSeconds(long wait) {
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(wait));
		return (T)this;
	}

	public T checkUrlLoadedCorrectly(String url) {
		waitDriver.until(ExpectedConditions.urlContains(url));
		assertThat(driver.getPageSource().toUpperCase().contains("INTERNAL SERVER ERROR"))
			.as("Page is not loaded correctly - INTERNAL SERVER ERROR occurred")
			.isEqualTo(false);
		return (T)this;
	}

	public void quit() {
		driver.quit();
	}

	public static class LoginPage extends FurmsUIDriver<LoginPage> {

		private LoginPage(WebDriver driver) {
			super(driver);
		}

		public LoginPage assertViewIsVisible() {
			return checkUrlLoadedCorrectly("oauth2-as/oauth2-authz-web-entry");
		}

		public LoginPage writeUsername(String username) {
			super.driver.findElements(By.tagName("input")).get(0).sendKeys(username);
			return this;
		}

		public LoginPage writePassword(String password) {
			super.driver.findElements(By.tagName("input")).get(1).sendKeys(password);
			return this;
		}

		public LandingPage logIn() {
			super.driver.findElement(By.xpath("//div[@class='v-slot v-slot-u-signInButton v-slot-u-passwordSignInButton']")).click();
			return new LandingPage(super.driver);
		}
	}

	public static class LandingPage extends FurmsUIDriver<LandingPage> {
		private static final int FENIX_ADMIN_VIEW_PAGE_ID = 0;
		private static final int SITE_ADMIN_VIEW_PAGE_ID = 1;
		private static final int COMMUNITY_ADMIN_VIEW_PAGE_ID = 2;
		private static final int PROJECT_ADMIN_VIEW_PAGE_ID = 3;
		private static final int USER_VIEW_PAGE_ID = 4;

		private LandingPage(WebDriver driver) {
			super(driver);
		}

		public LandingPage assertViewIsVisible() {
			checkUrlLoadedCorrectly("front/start/role/chooser");
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.tagName("vaadin-button")));
			return this;
		}

		public LandingPage verifyLandingPageRolesNumber(int rolesNumber) {
			assertThat(super.driver.findElements(By.tagName("vaadin-button")).size()).isEqualTo(rolesNumber);
			return this;
		}

		public FenixView visitFenixView(){
			super.driver.findElements(By.tagName("vaadin-button")).get(FENIX_ADMIN_VIEW_PAGE_ID).click();
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.className("furms-select")));
			return new FenixView(super.driver);
		}

		public SiteView visitSiteView(){
			super.driver.findElements(By.tagName("vaadin-button")).get(SITE_ADMIN_VIEW_PAGE_ID).click();
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.className("furms-select")));
			return new SiteView(super.driver);
		}

		public CommunityView visitCommunityView(){
			super.driver.findElements(By.tagName("vaadin-button")).get(COMMUNITY_ADMIN_VIEW_PAGE_ID).click();
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.className("furms-select")));
			return new CommunityView(super.driver);
		}

		public ProjectView visitProjectView(){
			super.driver.findElements(By.tagName("vaadin-button")).get(PROJECT_ADMIN_VIEW_PAGE_ID).click();
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.className("furms-select")));
			return new ProjectView(super.driver);
		}

		public UserView visitUserView(){
			super.driver.findElements(By.tagName("vaadin-button")).get(USER_VIEW_PAGE_ID).click();
			super.waitDriver.until(ExpectedConditions.elementToBeClickable(By.className("furms-select")));
			return new UserView(super.driver);
		}
	}

	public static abstract class MainView<Z extends FurmsUIDriver<Z>> extends FurmsUIDriver<Z> {
		private static final int FENIX_ADMIN_VIEW_SELECTOR_ID = 1;
		private static final int SITE_ADMIN_VIEW_SELECTOR_ID = 2;
		private static final int COMMUNITY_ADMIN_VIEW_SELECTOR_ID = 3;
		private static final int PROJECT_ADMIN_VIEW_SELECTOR_ID = 4;
		private static final int USER_SETTINGS_VIEW_SELECTOR_ID = 5;

		MainView(WebDriver driver) {
			super(driver);
		}

		public FenixView visitFenixView(){
			clickRoleInRolePicker(FENIX_ADMIN_VIEW_SELECTOR_ID);
			return new FenixView(super.driver);
		}

		public SiteView visitSiteView(){
			clickRoleInRolePicker(SITE_ADMIN_VIEW_SELECTOR_ID);
			return new SiteView(super.driver);
		}

		public CommunityView visitCommunityView(){
			clickRoleInRolePicker(COMMUNITY_ADMIN_VIEW_SELECTOR_ID);
			return new CommunityView(super.driver);
		}

		public ProjectView visitProjectView(){
			clickRoleInRolePicker(PROJECT_ADMIN_VIEW_SELECTOR_ID);
			return new ProjectView(super.driver);
		}

		public UserView visitUserView(){
			clickRoleInRolePicker(USER_SETTINGS_VIEW_SELECTOR_ID);
			return new UserView(super.driver);
		}

		private void clickRoleInRolePicker(int fenixAdminViewSelectorId) {
			super.driver.findElement(By.className("furms-select")).click();
			super.driver.findElements(By.tagName("vaadin-select-item")).get(fenixAdminViewSelectorId).click();
		}
	}

	public static class FenixView extends MainView<FenixView> {
		FenixView(WebDriver driver) {
			super(driver);
		}

		public FenixView assertFenixViewIsVisible() {
			return checkUrlLoadedCorrectly("fenix/admin/dashboard");
		}
	}

	public static class SiteView extends MainView<SiteView> {
		SiteView(WebDriver driver) {
			super(driver);
		}

		public SiteView assertSiteViewIsVisible() {
			return checkUrlLoadedCorrectly("site/admin/policy/document");
		}
	}

	public static class CommunityView extends MainView<CommunityView> {
		CommunityView(WebDriver driver) {
			super(driver);
		}

		public CommunityView assertCommunityViewIsVisible() {
			return checkUrlLoadedCorrectly("community/admin/dashboard");
		}
	}

	public static class ProjectView extends MainView<ProjectView> {
		ProjectView(WebDriver driver) {
			super(driver);
		}

		public ProjectView assertProjectViewIsVisible() {
			return checkUrlLoadedCorrectly("project/admin/users");
		}
	}

	public static class UserView extends MainView<UserView> {
		UserView(WebDriver driver) {
			super(driver);
		}

		public UserView assertUserViewIsVisible() {
			return checkUrlLoadedCorrectly("users/settings/profile");
		}
	}
}
