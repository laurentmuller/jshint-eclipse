/*******************************************************************************
 * Copyright (c) 2020 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent Muller - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint;

import java.io.File;

/**
 * Handler to output problems to the standard output stream.
 */
final class DebugProblemHandler implements ProblemHandler {

	private static final String FILE_PATTERN = "File: %s%n";

	private static final String PROBLEM_PATTERN = "%5s | %5s | %s%n";

	private File file;
	private File lastFile;

	public File getFile() {
		return file;
	}

	@Override
	public void handleProblem(final IProblem problem) {
		if (!file.equals(lastFile)) {
			outputHeaders();
			lastFile = file;
		}

		final int line = problem.getLine();
		final int offset = problem.getCharacter();
		final String message = problem.getMessage();
		System.out.format(PROBLEM_PATTERN, line, offset, message);

	}

	public void setFile(final File file) {
		this.file = file;
		lastFile = null;
	}

	private void outputHeaders() {
		outputLine();
		System.out.format(FILE_PATTERN, file);
		outputLine();
		System.out.format(PROBLEM_PATTERN, "Line", "Col.", "Message");
		outputLine();
	}

	private void outputLine() {
		System.out.println(
				"--------------------------------------------------------------------------------------------------------------------------------");
	}
}