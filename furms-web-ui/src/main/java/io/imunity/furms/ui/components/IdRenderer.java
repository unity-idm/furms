package io.imunity.furms.ui.components;

import com.vaadin.flow.data.renderer.BasicRenderer;
import com.vaadin.flow.function.ValueProvider;

import io.imunity.furms.domain.Id;

public class IdRenderer<SOURCE, ID extends Id> extends BasicRenderer<SOURCE, ID> {

	public IdRenderer(ValueProvider<SOURCE, ID> valueProvider) {
		super(valueProvider);
	}

	@Override
	protected String getFormattedValue(ID id) {
		return id.asRawString();
	}
}
