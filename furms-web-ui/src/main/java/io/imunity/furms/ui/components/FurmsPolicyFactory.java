/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class FurmsPolicyFactory {
	public static PolicyFactory create(){
		return Sanitizers.STYLES
			.and(Sanitizers.TABLES)
			.and(new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				.allowCommonBlockElements()
				.allowCommonInlineFormattingElements()
				.allowElements("a", "img", "audio", "video", "audio-wrapper", "video-wrapper")
				.allowAttributes("href", "src", "controls")
				.onElements("a", "img", "audio", "video")
				.toFactory()
			);
	}
}
