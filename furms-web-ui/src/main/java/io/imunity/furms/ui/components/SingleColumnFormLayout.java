/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */


package io.imunity.furms.ui.components;

import com.vaadin.flow.component.formlayout.FormLayout;

public class SingleColumnFormLayout extends FormLayout
{
	public SingleColumnFormLayout()
	{
		setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1));
	}
}
