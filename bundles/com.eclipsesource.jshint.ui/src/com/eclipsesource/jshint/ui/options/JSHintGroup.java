/*******************************************************************************
 * Copyright (c) 2018 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent Muller - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public final class JSHintGroup extends JSHintItem
		implements Iterable<JSHintOption> {

	private final JSHintRoot parent;
	private final List<JSHintOption> options;

	JSHintGroup(final JSHintRoot parent, final String name,
			final String description) {
		super(name, description);
		this.parent = parent;
		options = new ArrayList<>();
	}

	public List<JSHintOption> getOptions() {
		return options;
	}

	public JSHintRoot getParent() {
		return parent;
	}

	public boolean isEmpty() {
		return options.isEmpty();
	}

	@Override
	public Iterator<JSHintOption> iterator() {
		return options.iterator();
	}

	public void sort() {
		Collections.sort(options);
	}

	public JSHintOption[] toArray() {
		return options.toArray(new JSHintOption[options.size()]);
	}

	@Override
	public String toString() {
		return String.format("%s(%d)", name, options.size());
	}

	void add(final JsonObject json) {
		final String name = json.get("name").asString();
		final String description = json.get("description").asString();
		final JsonValue value = json.get("value");
		final JsonValue jsonDeprecated = json.get("deprecated");
		final String deprecated = jsonDeprecated != null
				? jsonDeprecated.asString()
				: null;

		final JSHintOption option = new JSHintOption(this, name, description,
				value, deprecated);
		options.add(option);
	}

}