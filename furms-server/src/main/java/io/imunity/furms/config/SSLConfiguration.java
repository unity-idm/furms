package io.imunity.furms.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;

@Component
public class SSLConfiguration
{
    @Autowired
    private Environment env;

    @PostConstruct
    private void configureSSL()
    {
        if (env.getProperty("server.ssl.trust-store") != null)
        {
            System.setProperty("javax.net.ssl.trustStore", env.getProperty("server.ssl.trust-store"));
        }
        if (env.getProperty("server.ssl.trust-store-password") != null)
        {
            System.setProperty("javax.net.ssl.trustStorePassword",
                    env.getProperty("server.ssl.trust-store-password"));
        }
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}
