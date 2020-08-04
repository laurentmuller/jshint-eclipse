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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.JsonObject;

public class JSHintRunner {

	/**
	 * Handler to output problems to the standard output stream.
	 */
	private static final class SysoutProblemHandler implements ProblemHandler {

		private final String file;

		public SysoutProblemHandler(final String file) {
			this.file = file;
		}

		@Override
		public void handleProblem(final Problem problem) {
			final int line = problem.getLine();
			final String message = problem.getMessage();
			System.out.format("Problem in file %s at line %s: %s%n", //
					file, line, message);
		}

	}

	/**
	 * The charset parameter name.
	 */
	private static final String PARAM_CHARSET = "--charset";

	/**
	 * The custom JSHINT parameter name.
	 */
	private static final String PARAM_CUSTOM_JSHINT = "--custom";

	/*
	 * the list of files to verify
	 */
	private List<File> files;

	/*
	 * the charset to use
	 */
	private Charset charset;

	/*
	 * the JSHint library file
	 */
	private File library;

	/*
	 * the JSHint instance
	 */
	private JSHint jshint;

	public void run(final String... args) {
		try {
			readArgs(args);
			ensureCharset();
			ensureInputFiles();
			loadJSHint();
			configureJSHint();
			processFiles();
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			System.out.println();
			System.out.println(
					"Usage: JSHint [ <options> ] <input-file> [ <input-file> ... ]");
			System.out.println("Options: --custom <custom-jshint-file>");
			System.out.println("         --charset <charset>");
		}
	}

	private File checkFile(final File file) throws IllegalArgumentException {
		if (!file.isFile()) {
			throw new IllegalArgumentException(
					"No such file: " + file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot read file: " + file.getAbsolutePath());
		}
		return file;
	}

	private void configureJSHint() {
		final JsonObject configuration = new JsonObject();
		configuration.add("undef", true);
		jshint.configure(configuration);
	}

	private void ensureCharset() {
		if (charset == null) {
			setCharset("UTF-8");
		}
	}

	private void ensureInputFiles() {
		if (files.isEmpty()) {
			throw new IllegalArgumentException("No input files.");
		}
	}

	private void loadJSHint() {
		jshint = new JSHint();

		if (library != null) {
			try (FileInputStream stream = new FileInputStream(library)) {
				jshint.load(stream);
			} catch (final IOException e) {
				final String msg = String
						.format("Failed to load JSHint library: %s.", library);
				throw new IllegalArgumentException(msg, e);
			}
		}
	}

	private void processFiles() throws IOException {
		for (final File file : files) {
			final String code = readContent(file);
			final ProblemHandler handler = new SysoutProblemHandler(
					file.getAbsolutePath());
			jshint.check(code, handler);
		}
	}

	private void readArgs(final String[] args) {
		String lastArg = null;
		files = new ArrayList<File>();

		for (final String arg : args) {
			if (PARAM_CHARSET.equalsIgnoreCase(lastArg)) {
				setCharset(arg);
			} else if (PARAM_CUSTOM_JSHINT.equalsIgnoreCase(lastArg)) {
				setLibrary(arg);
			} else if (PARAM_CHARSET.equalsIgnoreCase(arg)
					|| PARAM_CUSTOM_JSHINT.equalsIgnoreCase(arg)) {
				// continue
			} else {
				final File file = new File(arg);
				files.add(checkFile(file));
			}
			lastArg = arg;
		}
	}

	private String readContent(final File file) throws IOException {
		try (final InputStream stream = new FileInputStream(file);
				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(stream, charset))) {

			String line;
			final StringBuilder builder = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append('\n');
			}
			return builder.toString();
		}
	}

	private void setCharset(final String name) {
		try {
			charset = Charset.forName(name);
		} catch (final Exception e) {
			final String msg = String
					.format("Unknown or unsupported charset: %s.", name);
			throw new IllegalArgumentException(msg, e);
		}
	}

	private void setLibrary(final String name) {
		library = new File(name);
	}
}
