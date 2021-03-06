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
import java.util.Objects;

@SuppressWarnings("serial") // use default serial UID
public class JsonString extends JsonValue {

	private final String string;

	JsonString(final String string) {
		Objects.requireNonNull(string, "The 'string' parameter is null.");
		this.string = string;
	}

	@Override
	public String asString() {
		return string;
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
		final JsonString other = (JsonString) object;
		return string.equals(other.string);
	}

	@Override
	public int hashCode() {
		return string.hashCode();
	}

	@Override
	public boolean isString() {
		return true;
	}

	@Override
	protected void write(final JsonWriter writer) throws IOException {
		writer.writeString(string);
	}

}
