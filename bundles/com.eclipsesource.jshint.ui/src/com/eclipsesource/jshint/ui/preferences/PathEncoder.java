/*******************************************************************************
 * Copyright (c) 2012 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences;

import java.util.ArrayList;
import java.util.List;

public class PathEncoder {

	public static List<String> decodePaths(final String encodedPaths) {
		final List<String> list = new ArrayList<>();
		for (final String path : encodedPaths.split(":")) {
			if (path.length() > 0) {
				list.add(path);
			}
		}
		return list;
	}

	public static String encodePaths(final List<String> paths) {
		final StringBuilder builder = new StringBuilder();
		for (final String path : paths) {
			if (path.length() > 0) {
				if (builder.length() > 0) {
					builder.append(':');
				}
				builder.append(path);
			}
		}
		return builder.toString();
	}
}
