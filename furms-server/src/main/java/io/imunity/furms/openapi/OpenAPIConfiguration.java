package io.imunity.furms.openapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition 
@Component
class OpenAPIConfiguration
{
	@Bean
	OpenAPI customOpenAPI(@Value("${app.version:unknown}") String version) 
	{
		SecurityScheme cidpSecScheme =  new SecurityScheme()
				.name(APIDocConstants.CIDP_SECURITY_SCHEME)
				.type(SecurityScheme.Type.HTTP)
				.scheme("bearer")
				.description("Pre-shared token between Fenix central IdP and FURMS shall be used "
						+ "for authentication and authorization of each request");
		
		return new OpenAPI()
				.info(new Info()
						.title("FURMS REST API")
						.version(version)
						.license(new License().name("BSD").url("https://opensource.org/licenses/BSD-2-Clause")))
				.addSecurityItem(new SecurityRequirement().addList(APIDocConstants.CIDP_SECURITY_SCHEME))
				.components(new Components()
						.addSecuritySchemes(APIDocConstants.CIDP_SECURITY_SCHEME, cidpSecScheme));
	}
}
