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
package com.eclipsesource.jshint;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Objects;

/**
 * Wrapper class for the text, used to read the content of a text file and to
 * track line offsets.
 */
public class Text {

	private static final int LINE_FEED = '\n';

	private static final int CARRIAGE_RETURN = '\r';

	private String content;
	private int lineCount = 1;
	private int[] lineOffsets = new int[200];

	public Text(final Reader reader) throws IOException {
		Objects.requireNonNull(reader, "The 'reader' parameter is null.");
		read(reader);
	}

	public Text(final String text) {
		Objects.requireNonNull(text, "The 'text' parameter is null.");
		try (StringReader reader = new StringReader(text)) {
			read(reader);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getContent() {
		return content;
	}

	public int getLineCount() {
		return lineCount;
	}

	/**
	 * Returns the length of the given line, including line break characters.
	 *
	 * @param line
	 *            the zero-relative line index.
	 * @return the line length in characters
	 * @throws IndexOutOfBoundsException
	 *             if the line is smaller than 0 or greater or equal to the line
	 *             count.
	 */
	public int getLineLength(final int line) {
		checkLineIndex(line);
		final int nextOffset = line + 1 == lineCount ? content.length()
				: lineOffsets[line + 1];
		return nextOffset - lineOffsets[line];
	}

	/**
	 * Returns the offset of the given line's first character.
	 *
	 * @param line
	 *            the zero-relative line index.
	 * @return the line offset.
	 * @throws IndexOutOfBoundsException
	 *             if the line is smaller than 0 or greater or equal to the line
	 *             count.
	 */
	public int getLineOffset(final int line) {
		checkLineIndex(line);
		return lineOffsets[line];
	}

	private void addLineOffset(final int offset) {
		final int len = lineOffsets.length;
		if (lineCount >= len) {
			lineOffsets = Arrays.copyOf(lineOffsets, len + 100);
		}
		lineOffsets[lineCount++] = offset;
	}

	private void checkLineIndex(final int line) {
		if (line < 0 || line >= lineCount) {
			throw new IndexOutOfBoundsException(
					String.format("The line %s does not exist.", line));
		}
	}

	private void read(final Reader reader) throws IOException {
		int ch;
		int previous = 0;
		final StringBuffer buffer = new StringBuffer(8096);

		while ((ch = reader.read()) != -1) {
			if (ch == LINE_FEED) {
				// handle Linux (LF) and Windows (CR+LF)
				addLineOffset(buffer.length() + 1);

			} else if (previous == CARRIAGE_RETURN && ch != LINE_FEED) {
				// handle Mac (CR)
				addLineOffset(buffer.length());
			}
			buffer.append((char) ch);
			previous = ch;
		}

		// check last character (Mac)
		if (previous == CARRIAGE_RETURN) {
			addLineOffset(buffer.length());
		}

		content = buffer.toString();
		if (content.isEmpty()) {
			lineCount = 0;
		}
	}
}
