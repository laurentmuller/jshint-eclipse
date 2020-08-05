/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class OptionParserUtils {

	private static final class Entry {
		public final String name;
		public final JsonValue value;

		public Entry(final String name, final JsonValue value) {
			this.name = name;
			this.value = value;
		}
	}

	public static JsonObject createConfiguration(final String options,
			final String globals) throws IOException {
		final JsonObject configuration = new JsonObject();
		for (final Entry entry : parseOptionString(options)) {
			configuration.add(entry.name, entry.value);
		}
		final JsonObject globalsObject = new JsonObject();
		for (final Entry entry : parseOptionString(globals)) {
			globalsObject.add(entry.name, entry.value == JsonValue.TRUE);
		}
		if (!globalsObject.isEmpty()) {
			configuration.add("globals", globalsObject);
		}
		return configuration;
	}

	private static String parseOptionElement(final List<Entry> result,
			final String element) throws IOException {
		if (!element.isEmpty()) {
			final String[] parts = element.split(":", 2);
			final String key = parts[0].trim();
			if (!key.isEmpty()) {
				if (parts.length == 2) {
					final JsonValue value = JsonValue.readFrom(parts[1].trim());
					result.add(new Entry(key, value));
				} else {
					throw new IOException(
							"The 'parts' must contains 2 elements.");
				}
			}
		}
		return element;
	}

	private static List<Entry> parseOptionString(final String input)
			throws IOException {
		final List<Entry> result = new ArrayList<>();
		final String[] elements = input.split(",");
		for (String element : elements) {
			element = parseOptionElement(result, element.trim());
		}
		return result;
	}

	/*
	 * prevent instance creation
	 */
	private OptionParserUtils() {
		throw new AssertionError("No OptionParserUtils instances is allowed"); //$NON-NLS-1$
	}

}
