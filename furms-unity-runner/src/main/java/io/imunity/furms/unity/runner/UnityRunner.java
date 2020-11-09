/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.unity.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import pl.edu.icm.unity.MessageSource;
import pl.edu.icm.unity.engine.PKIManagementImpl;
import pl.edu.icm.unity.engine.server.UnityApplication;
import pl.edu.icm.unity.rest.jwt.authn.JWTVerificator;

@SpringBootApplication
public class UnityRunner
{
	@Bean
	JWTVerificator addVerify(){
		return new JWTVerificator(null);
	}

	public static void main(String[] args)
	{
		UnityApplication theServer = new UnityApplication(MessageSource.PROFILE_FAIL_ON_MISSING);
		theServer.run(new String[] {"src/main/resources/unityServer.conf"});
	}
}
