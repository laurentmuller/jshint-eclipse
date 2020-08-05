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
package com.eclipsesource.jshint;

public class Problem implements IProblem {

	private final int line;
	private final int character;
	private final String message;
	private final String code;

	public Problem(final int line, final int character,
			final String message, final String code) {
		this.line = line;
		this.character = character;
		this.message = message;
		this.code = code;
	}

	@Override
	public int getCharacter() {
		return character;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public boolean isError() {
		return code != null && !code.isEmpty() && code.charAt(0) == 'E';
	}
}
