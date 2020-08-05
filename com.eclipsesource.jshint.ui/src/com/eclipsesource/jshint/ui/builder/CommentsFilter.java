/*******************************************************************************
 * Copyright (c) 2013, 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.builder;

public final class CommentsFilter {

	private static final char CARRIAGE_RETURN = '\r';
	private static final char LINE_FEED = '\n';
	private static final char COMMENT = '/';
	private static final char ASTERIX = '*';
	private static final char SPACE = ' ';

	public static String filter(final String input) {
		char lastCh = 0;
		boolean inLineComment = false;
		boolean inBlockComment = false;

		char ch;
		final char[] chars = input.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			ch = chars[i];
			if (inLineComment) {
				if (ch == CARRIAGE_RETURN || ch == LINE_FEED) {
					inLineComment = false;
				} else {
					chars[i] = SPACE;
				}

			} else if (inBlockComment) {
				if (lastCh == ASTERIX && ch == COMMENT) { // "*/"
					inBlockComment = false;
				}
				chars[i] = chars[i] == LINE_FEED ? LINE_FEED : SPACE;

			} else if (lastCh == COMMENT && ch == COMMENT) {
				inLineComment = true;
				chars[i - 1] = SPACE;
				chars[i] = SPACE;

			} else if (lastCh == COMMENT && ch == ASTERIX) { // "/*"
				inBlockComment = true;
				chars[i - 1] = SPACE;
				chars[i] = SPACE;
			}
			lastCh = ch;
		}

		return new String(chars);
	}

	private CommentsFilter() {
	}
}
