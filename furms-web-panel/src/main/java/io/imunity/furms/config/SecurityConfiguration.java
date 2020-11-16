package io.imunity.furms.config;

import com.vaadin.flow.shared.ApplicationConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import static com.vaadin.flow.server.ServletHelper.*;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{

    private static final String LOGIN_URL = "/front/login";
    private static final String LOGIN_SUCCESS_URL = "/front/hello";
    private static final String LOGOUT_URL = "/logout";
    private static final String LOGOUT_SUCCESS_URL = "/front/hello";

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
                .and().oauth2Login().loginPage(LOGIN_URL).defaultSuccessUrl(LOGIN_SUCCESS_URL).permitAll();
    }

    @Override
    public void configure(WebSecurity web)
    {
        web.ignoring().antMatchers(
                // client-side JS code
                "/VAADIN/**","/front/VAADIN/**",

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
}