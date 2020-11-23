/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.unity.runner;

import pl.edu.icm.unity.MessageSource;
import pl.edu.icm.unity.engine.server.UnityApplication;

public class UnityRunner
{
	public static void main(String[] args)
	{
		UnityApplication theServer = new UnityApplication(MessageSource.PROFILE_FAIL_ON_MISSING);
		theServer.run(new String[] {"src/test/resources/unityServer.conf"});
	}
}