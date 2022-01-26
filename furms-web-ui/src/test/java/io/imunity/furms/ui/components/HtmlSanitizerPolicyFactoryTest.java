/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.owasp.html.PolicyFactory;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlSanitizerPolicyFactoryTest {

	private PolicyFactory policyFactory;

	@BeforeEach
	void setUp() {
		policyFactory = HtmlSanitizerPolicyFactory.create();
	}

	@Test
	void shouldAllowImgTag() {
		String html = "<img src=\"www.google.com\" />";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowAudioTag() {
		String html = "<audio-wrapper><audio src=\"www.google.com\" controls=\"\"></audio></audio-wrapper>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowVideoTag() {
		String html = "<video-wrapper><video src=\"www.google.com\" controls=\"\"></video></video-wrapper>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowH1Tag() {
		String html = "<h1>Test</h1>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowH6Tag() {
		String html = "<h6>Test</h6>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowBTag() {
		String html = "<b>asdsadasd</b>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowTable() {
		String html = "<ol><li>sadasdasdasddasdasdsad</li></ol>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}

	@Test
	void shouldAllowATag() {
		String html = "<a href=\"www.google.com\">link text</a>";
		String safeHtml = policyFactory.sanitize(html);

		assertThat(safeHtml).isEqualTo(html);
	}
}