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

import com.eclipsesource.json.JsonValue;

public final class JSHintOption extends JSHintItem {

	private final JSHintGroup parent;
	private final JsonValue value;
	private final String deprecated;

	JSHintOption(final JSHintGroup parent, final String name,
			final String description, final JsonValue value,
			final String deprecated) {
		super(name, description);
		this.parent = parent;
		this.value = value;
		this.deprecated = deprecated;
	}

	/**
	 * Returns this JSON value as a <code>boolean</code> value, assuming that
	 * this value is either <code>true</code> or <code>false</code>. If this is
	 * not the case, an exception is thrown.
	 *
	 * @return this value as <code>boolean</code>
	 * @throws UnsupportedOperationException
	 *             if this value is neither <code>true</code> or
	 *             <code>false</code>
	 */
	public boolean asBoolean() {
		return value.asBoolean();
	}

	/**
	 * Returns this JSON value as an <code>int</code> value, assuming that this
	 * value represents a JSON number that can be interpreted as Java
	 * <code>int</code>. If this is not the case, an exception is thrown.
	 * <p>
	 * To be interpreted as Java <code>int</code>, the JSON number must neither
	 * contain an exponent nor a fraction part. Moreover, the number must be in
	 * the <code>Integer</code> range.
	 * </p>
	 *
	 * @return this value as <code>int</code>
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON number
	 * @throws NumberFormatException
	 *             if this JSON number can not be interpreted as
	 *             <code>int</code> value
	 */
	public int asInt() {
		return value.asInt();
	}

	public String asJson() {
		return String.format("\"%s\": %s", name, value);
	}

	/**
	 * Returns this JSON value as String, assuming that this value represents a
	 * JSON string. If this is not the case, an exception is thrown.
	 *
	 * @return the string represented by this value
	 * @throws UnsupportedOperationException
	 *             if this value is not a JSON string
	 */
	public String asString() {
		return value.asString();
	}

	public String getDeprecated() {
		return deprecated;
	}

	public JSHintGroup getParent() {
		return parent;
	}

	@Override
	public String getTooltip() {
		if (isDeprecated()) {
			return super.getTooltip() + "\n\n"
					+ wordwrap("Deprecated: " + deprecated);
		}
		return super.getTooltip();
	}

	public JsonValue getValue() {
		return value;
	}

	/**
	 * Detects whether this value represents a boolean value.
	 *
	 * @return <code>true</code> if this value represents either the JSON
	 *         literal <code>true</code> or <code>false</code>
	 */
	public boolean isBoolean() {
		return value.isBoolean();
	}

	/**
	 * Returns if this option is deprecated.
	 *
	 * @return <code>true</code> if deprecated.
	 */
	public boolean isDeprecated() {
		return deprecated != null && !deprecated.isEmpty();
	}

	/**
	 * Detects whether this value represents a JSON number.
	 *
	 * @return <code>true</code> if this value represents a JSON number
	 */
	public boolean isNumber() {
		return value.isNumber();
	}

	/**
	 * Detects whether this value represents a JSON string.
	 *
	 * @return <code>true</code> if this value represents a JSON string
	 */
	public boolean isString() {
		return value.isString();
	}

	@Override
	public String toString() {
		return String.format("%s=%s", name, value);
	}

}