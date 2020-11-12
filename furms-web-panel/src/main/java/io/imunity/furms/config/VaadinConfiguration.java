package io.imunity.furms.config;

import com.vaadin.flow.spring.SpringServlet;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableVaadin("io.imunity.furms")
public class VaadinConfiguration
{
    @Bean
    public ServletRegistrationBean<SpringServlet> configVaadinMapping(ApplicationContext context) {
        return new ServletRegistrationBean<>(new SpringServlet(context, false), "/front/*", "/front/*");
    }
}
