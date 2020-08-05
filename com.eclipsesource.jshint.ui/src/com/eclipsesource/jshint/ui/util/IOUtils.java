/*******************************************************************************
 * Copyright (c) 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import com.eclipsesource.jshint.ui.Activator;

public class IOUtils {

	public static final String UTF_8 = "UTF-8";

	public static ByteArrayInputStream createUtf8InputStream(
			final String string) throws IOException {
		return new ByteArrayInputStream(string.getBytes(UTF_8));
	}

	public static BufferedReader createUtf8Reader(final InputStream stream)
			throws IOException {
		return new BufferedReader(new InputStreamReader(stream, UTF_8));
	}

	public static String readUtf8File(final File file) throws IOException {
		try (InputStream stream = new BufferedInputStream(
				new FileInputStream(file))) {
			return readUtf8String(stream);
		}
	}

	public static String readUtf8File(final IFile file) throws CoreException {
		if (file.isAccessible()) {
			try (InputStream stream = file.getContents(true)) {
				return readUtf8String(stream);
			} catch (final IOException e) {
				final String msg = String.format(
						"Unable to read the content from '%s'.",
						file.getName());
				throw Activator.createException(msg, e);
			}
		}
		return null;
	}

	public static String readUtf8String(final InputStream stream)
			throws IOException {
		int read;
		final char[] buffer = new char[4096];
		final StringBuilder builder = new StringBuilder();

		try (BufferedReader reader = createUtf8Reader(stream)) {
			while ((read = reader.read(buffer)) != -1) {
				builder.append(buffer, 0, read);
			}
		}
		return builder.toString();
	}

	public static void writeUtf8File(final File file, final String content)
			throws IOException {
		try (OutputStream outputStream = new BufferedOutputStream(
				new FileOutputStream(file))) {
			outputStream.write(content.getBytes(UTF_8));
		}
	}

	public static void writeUtf8File(final IFile file, final String content)
			throws CoreException {
		try (InputStream stream = createUtf8InputStream(content)) {
			if (file.isAccessible()) {
				file.setContents(stream, true, true, null);
			} else {
				file.create(stream, true, null);
				file.setCharset(UTF_8, null);
			}
		} catch (final IOException e) {
			final String msg = String.format(
					"Unable to write the content to '%s'.", file.getName());
			throw Activator.createException(msg, e);
		}
	}

	/*
	 * prevent instance creation
	 */
	private IOUtils() {
		throw new AssertionError("No IOUtils instances is allowed"); //$NON-NLS-1$
	}
}
