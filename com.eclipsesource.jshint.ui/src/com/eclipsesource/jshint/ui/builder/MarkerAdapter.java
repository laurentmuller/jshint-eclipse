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
package com.eclipsesource.jshint.ui.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerAdapter {

	public static final String TYPE_PROBLEM = "com.eclipsesource.jshint.ui.problemmarker";

	private final IResource resource;

	public MarkerAdapter(final IResource resource) {
		this.resource = resource;
	}

	public void createError(final int line, final int start, final int end,
			final String message) throws CoreException {
		createMarker(line, start, end, message, IMarker.SEVERITY_ERROR);
	}

	public void createWarning(final int line, final int start, final int end,
			final String message) throws CoreException {
		createMarker(line, start, end, message, IMarker.SEVERITY_WARNING);
	}

	public void removeMarkers() throws CoreException {
		resource.deleteMarkers(TYPE_PROBLEM, true, IResource.DEPTH_INFINITE);
	}

	private void createMarker(final int line, final int start, final int end,
			final String message, final int severity) throws CoreException {
		if (message == null) {
			throw new NullPointerException("The marker's message is null");
		}
		final IMarker marker = resource.createMarker(TYPE_PROBLEM);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.MESSAGE, message);
		if (line >= 1) {
			// needed to display line number in problems view location column
			marker.setAttribute(IMarker.LINE_NUMBER, line);
		}
		if (start >= 0) {
			marker.setAttribute(IMarker.CHAR_START, start);
			marker.setAttribute(IMarker.CHAR_END, Math.max(start, end));
		}
	}
}
