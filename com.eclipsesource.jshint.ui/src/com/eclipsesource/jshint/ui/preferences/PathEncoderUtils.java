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

public class PathEncoderUtils {

	/*
	 * the paths separator
	 */
	private static final String PATH_SEPARATOR = "#";

	public static List<String> decodePaths(final String encodedPaths) {
		final String[] paths = encodedPaths.split(PATH_SEPARATOR);
		final List<String> list = new ArrayList<>(paths.length);

		for (final String path : paths) {
			if (!path.isEmpty()) {
				list.add(path);
			}
		}
		return list;
	}

	public static String encodePaths(final List<String> paths) {
		boolean addSeparator = false;
		final StringBuilder builder = new StringBuilder(256);

		for (final String path : paths) {
			if (path != null && !path.isEmpty()) {
				if (addSeparator) {
					builder.append(PATH_SEPARATOR);
				}
				builder.append(path);
				addSeparator = true;
			}
		}
		return builder.toString();
	}

	/*
	 * prevent instance creation
	 */
	private PathEncoderUtils() {
		throw new AssertionError("No PathEncoderUtils instances is allowed"); //$NON-NLS-1$
	}
}
