/*******************************************************************************
 * Copyright (c) 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.json;

import java.io.IOException;

@SuppressWarnings("serial") // use default serial UID
public class JsonNumber extends JsonValue {

	private final String string;

	JsonNumber(final String string) {
		if (string == null) {
			throw new NullPointerException("string is null");
		}
		this.string = string;
	}

	@Override
	public double asDouble() {
		return Double.parseDouble(string);
	}

	@Override
	public float asFloat() {
		return Float.parseFloat(string);
	}

	@Override
	public int asInt() {
		return Integer.parseInt(string, 10);
	}

	@Override
	public long asLong() {
		return Long.parseLong(string, 10);
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}
		if (object == null) {
			return false;
		}
		if (getClass() != object.getClass()) {
			return false;
		}
		final JsonNumber other = (JsonNumber) object;
		return string.equals(other.string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public String toString() {
		return string;
	}

	@Override
	protected void write(final JsonWriter writer) throws IOException {
		writer.write(string);
	}

}
