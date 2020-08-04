/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Objects;

import com.eclipsesource.jshint.ui.builder.CommentsFilter;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.PrettyPrintJsonWriter;

public class JsonUtil {

	public static boolean jsonEquals(final String str1, final String str2) {
		if (Objects.equals(str1, str2)) {
			return true;
		} else if (str1 != null && str2 != null) {
			try {
				final JsonObject json1 = readFrom(str1);
				final JsonObject json2 = readFrom(str2);
				return json1.equals(json2);
			} catch (final Exception e) {
				// ignore
			}
		}
		return false;
	}

	public static String prettyPrint(final JsonObject obj)
			throws RuntimeException {
		try {
			final StringWriter writer = new StringWriter();
			obj.writeTo(new PrettyPrintJsonWriter(writer));
			return writer.toString();

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String prettyPrint(final String str) throws RuntimeException {
		final JsonObject obj = readFrom(str);
		return prettyPrint(obj);
	}

	public static JsonObject readFrom(final String str) {
		return JsonObject.readFrom(CommentsFilter.filter(str));
	}
}
