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

import java.util.Objects;

public abstract class JSHintItem implements Comparable<JSHintItem> {

	protected final String name;
	protected final String description;

	JSHintItem(final String name, final String description) {
		this.name = name;
		this.description = description;
	}

	@Override
	public int compareTo(final JSHintItem o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final JSHintItem other = (JSHintItem) obj;
		return Objects.equals(name, other.name);
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getTooltip() {
		return wordwrap(description);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(name);
	}

	@Override
	public String toString() {
		return name;
	}

	protected String wordwrap(final String input) {
		if (input == null || input.isEmpty()) {
			return input;
		}

		final int max_length = 75;
		final int length = input.length();
		if (length > max_length) {
			int len = 0;
			final String[] lines = input.split(" ");
			final StringBuffer buffer = new StringBuffer(length + 10);
			for (final String line : lines) {
				len += line.length();
				if (len >= max_length) {
					buffer.append('\n');
					len = 0;
				} else if (buffer.length() > 0) {
					buffer.append(' ');
				}
				buffer.append(line);
			}
			return buffer.toString();
		} else {
			return input;
		}
	}
}