/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.config;

import com.vaadin.flow.shared.ApplicationConstants;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.endpoint.*;
import org.springframework.web.client.RestTemplate;


import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import static com.vaadin.flow.server.ServletHelper.*;
import static io.imunity.furms.constant.LoginFlowConst.*;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	private static final String LOGOUT_SUCCESS_URL = "/front/hello";

	@Value("${server.ssl.trust-store}")
	private Resource keyStore;
	@Value("${server.ssl.trust-store-password}")
	private String keyStorePassword;

	@Override
	protected void configure(HttpSecurity http) throws Exception
	{
		http
				// Allow all flow internal requests.
				.authorizeRequests().requestMatchers(SecurityConfiguration::isFrameworkInternalRequest).permitAll()

				// Restrict access to our application.
				.and().authorizeRequests().anyRequest().authenticated()

				// Not using Spring CSRF here to be able to use plain HTML for the login page
				.and().csrf().disable()

				// Configure logout
				.logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)

				// Configure the login page.
				.and().oauth2Login().loginPage(LOGIN_URL).defaultSuccessUrl(LOGIN_SUCCESS_URL).permitAll()

				// Configure RestClient.
				.and().oauth2Login().tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient());
	}

	@Override
	public void configure(WebSecurity web)
	{
		web.ignoring().antMatchers(
				// client-side JS code
				"/VAADIN/**", "/front/VAADIN/**",

				// the standard favicon URI
				"/front/favicon.ico",

				// web application manifest
				"/front/manifest.webmanifest", "/front/sw.js", "/front/offline-page.html",

				// icons and images
				"/front/icons/**", "/front/images/**");
	}

	static boolean isFrameworkInternalRequest(HttpServletRequest request)
	{
		final String parameterValue = request
				.getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
		return parameterValue != null &&
				Stream.of(RequestType.values())
						.anyMatch(r -> r.getIdentifier().equals(parameterValue));
	}

	private OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient(){
		DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient = new DefaultAuthorizationCodeTokenResponseClient();
		RestTemplate restClientForUnityConnection = getRestClientForUnityConnection();
		accessTokenResponseClient.setRestOperations(restClientForUnityConnection);
		return accessTokenResponseClient;
	}

	private RestTemplate getRestClientForUnityConnection()
	{
		SSLContext sslContext;
		try
		{
			sslContext = new SSLContextBuilder()
					.loadTrustMaterial(keyStore.getFile(), keyStorePassword.toCharArray())
					.build();

		} catch (Exception e)
		{
			throw new RuntimeException(e.getMessage());
		}
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return new RestTemplate(factory);
	}
}