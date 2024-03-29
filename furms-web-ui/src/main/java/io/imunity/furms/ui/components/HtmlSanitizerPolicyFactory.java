/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlSanitizerPolicyFactory {
	public static PolicyFactory create(){
		return Sanitizers.STYLES
			.and(Sanitizers.TABLES)
			.and(new HtmlPolicyBuilder()
				.allowStandardUrlProtocols()
				.allowCommonBlockElements()
				.allowCommonInlineFormattingElements()
				.allowElements("a", "img", "audio", "video", "audio-wrapper", "video-wrapper", "center")
				.allowAttributes("href", "src", "controls", "target")
				.onElements("a", "img", "audio", "video")
				.allowAttributes("align")
				.onElements("p", "div", "h1", "h2", "h3", "h4", "h5", "h6")
				.toFactory()
			);
	}
}
