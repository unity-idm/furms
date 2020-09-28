/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/hello")
public class HelloWorldController
{
	@GetMapping
	public String sayHello()
	{
		return "Hello world!";
	}
}
