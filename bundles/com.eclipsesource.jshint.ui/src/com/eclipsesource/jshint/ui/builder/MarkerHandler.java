/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.builder;

import org.eclipse.core.runtime.CoreException;

import com.eclipsesource.jshint.Problem;
import com.eclipsesource.jshint.ProblemHandler;
import com.eclipsesource.jshint.Text;
import com.eclipsesource.jshint.ui.builder.JSHintBuilder.CoreExceptionWrapper;
import com.eclipsesource.jshint.ui.preferences.JSHintPreferences;

final class MarkerHandler implements ProblemHandler {

	private final MarkerAdapter adapter;
	private final Text code;
	private final boolean enableError;

	MarkerHandler(final MarkerAdapter adapter, final Text code) {
		this.adapter = adapter;
		this.code = code;
		enableError = new JSHintPreferences().getEnableErrorMarkers();
	}

	@Override
	public void handleProblem(final Problem problem) {
		final int line = problem.getLine();
		final int ch = problem.getCharacter();
		if (isValidLine(line)) {
			int offset = -1;
			if (isValidCharacter(line - 1, ch)) {
				offset = code.getLineOffset(line - 1) + ch;
			}
			createMarker(line, offset, problem.getMessage(), problem.isError());
		} else {
			createMarker(-1, -1, problem.getMessage(), problem.isError());
		}
	}

	private void createMarker(final int line, final int character,
			final String message, final boolean isError)
			throws CoreExceptionWrapper {
		try {
			if (enableError && isError) {
				adapter.createError(line, character, character, message);
			} else {
				adapter.createWarning(line, character, character, message);
			}
		} catch (final CoreException e) {
			throw new CoreExceptionWrapper(e);
		}
	}

	private boolean isValidCharacter(final int line, final int character) {
		return character >= 0 && character <= code.getLineLength(line);
	}

	private boolean isValidLine(final int line) {
		return line >= 1 && line <= code.getLineCount();
	}
}
