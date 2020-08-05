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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.osgi.service.prefs.Preferences;

public class ResourceSelector {

	private static List<PathPattern> createPatterns(
			final List<String> expressions) {
		final List<PathPattern> patterns = new ArrayList<>(expressions.size());
		for (final String expression : expressions) {
			patterns.add(PathPattern.create(expression));
		}
		return patterns;
	}

	private final List<PathPattern> includePatterns;

	private final List<PathPattern> excludePatterns;

	public ResourceSelector(final IProject project) {
		final Preferences preferenceNode = PreferencesFactoryUtils
				.getProjectPreferences(project);
		final EnablementPreferences preferences = new EnablementPreferences(
				preferenceNode);
		includePatterns = createPatterns(preferences.getIncludePatterns());
		excludePatterns = createPatterns(preferences.getExcludePatterns());
	}

	public boolean allowVisitFile(final IResource resource) {
		final String[] pathSegments = resource.getParent()
				.getProjectRelativePath().segments();
		final String fileName = resource.getName();
		return isFileIncluded(pathSegments, fileName)
				&& !isFileExcluded(pathSegments, fileName);
	}

	public boolean allowVisitFolder(final IResource resource) {
		return !includePatterns.isEmpty();
	}

	public boolean allowVisitProject() {
		return !includePatterns.isEmpty();
	}

	private boolean isFileExcluded(final String[] parentSegments,
			final String fileName) {
		for (final PathPattern pattern : excludePatterns) {
			if (pattern.matchesFolder(parentSegments)) {
				if (pattern.matchesFile(fileName)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isFileIncluded(final String[] parentSegments,
			final String fileName) {
		for (final PathPattern pattern : includePatterns) {
			if (pattern.matchesFolder(parentSegments)) {
				if (pattern.matchesFile(fileName)) {
					return true;
				}
			}
		}
		return false;
	}

}
