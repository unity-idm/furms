/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.ui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.WrappedHttpSession;

public class UIInSessionHolder
{
	private static final String SESSION_ATTR = UIInSessionHolder.class.getCanonicalName();
	
	public static void addUIToSession(UI ui, WrappedHttpSession session) {
		synchronized(session.getHttpSession()) {
			@SuppressWarnings("unchecked")
			List<UI> uiList = (List<UI>) session.getAttribute(SESSION_ATTR);
			if (uiList == null) {
				uiList = new ArrayList<>();
				session.setAttribute(SESSION_ATTR, uiList);
			}
			uiList.add(ui);
		}
	}
	
	public static List<UI> getUIsFromSession(HttpSession session) {
		synchronized(session) {
			@SuppressWarnings("unchecked")
			List<UI> uiList = (List<UI>) session.getAttribute(SESSION_ATTR);
			return uiList == null ? Collections.emptyList() : List.copyOf(uiList);
		}		
	}
}
