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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

public final class JSHintRoot implements Iterable<JSHintGroup> {

	/*
	 * the singleton instance
	 */
	private static volatile JSHintRoot instance = null;

	/**
	 * Returns the singleton instance.
	 *
	 * @return the singleton instance.
	 */
	public synchronized static JSHintRoot getInstance() {
		// double check locking
		if (instance == null) {
			synchronized (JSHintRoot.class) {
				if (instance == null) {
					instance = new JSHintRoot();
				}
			}
		}
		return instance;
	}

	/**
	 * Gets the reader for the java script options.
	 */
	private static Reader createReader() throws IOException {
		final Class<?> clazz = JSHintRoot.class;
		final InputStream stream = clazz.getResourceAsStream("options.json");
		return new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	}

	private final List<JSHintGroup> groups;

	private JSHintRoot() {
		groups = new ArrayList<>();

		String name;
		String description;
		JsonObject jsonGroup;

		try (final Reader reader = createReader()) {
			final JsonObject root = JsonObject.readFrom(createReader());

			for (final Member member : root) {
				// group
				name = member.getName();
				jsonGroup = member.getValue().asObject();
				description = jsonGroup.get("description").asString();
				final JSHintGroup group = new JSHintGroup(this, name,
						description);

				// options
				final JsonArray jsonOptions = jsonGroup.get("options")
						.asArray();
				for (final JsonValue jsonOption : jsonOptions) {
					group.add(jsonOption.asObject());
				}

				groups.add(group);
			}

		} catch (final IOException e) {
			// ignore
			e.printStackTrace();
		}
	}

	public boolean isEmpty() {
		return groups.isEmpty();
	}

	@Override
	public Iterator<JSHintGroup> iterator() {
		return groups.iterator();
	}

	public void sort() {
		Collections.sort(groups);
	}

	public JSHintGroup[] toArray() {
		return groups.toArray(new JSHintGroup[groups.size()]);
	}

	@Override
	public String toString() {
		return String.format("%s(%d)", //
				getClass().getSimpleName(), groups.size());
	}
}
