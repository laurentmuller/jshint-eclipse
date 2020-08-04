/*******************************************************************************
 * Copyright (c) 2012, 2014 EclipseSource.
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Lightweight Java wrapper for the JSHint code analysis tool.
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * JSHint jshint = new JSHint();
 * jshint.load();
 * jshint.configure( new Configuration() );
 * jshint.check( jsCode, new ProblemHandler() { ... } );
 * </pre>
 *
 * @see http://www.jshint.com/
 */
public class JSHint {

	/**
	 * The JSHint documentation page.
	 */
	public static final String DOC_URL = "https://jshint.com/docs";

	/**
	 * The JSHint library version.
	 */
	private static final String DEFAULT_JSHINT_VERSION = "2.11.0";

	/*
	 * the default JSON indentation
	 */
	private static final int DEFAULT_JSHINT_INDENT = 4;

	/**
	 * Returns the version of the built-in JSHint library that is used when
	 * <code>load()</code> is called without a parameter.
	 *
	 * @return the version name of the default JSHint version
	 */
	public static String getDefaultLibraryVersion() {
		return DEFAULT_JSHINT_VERSION;
	}

	public static void main(final String[] args) {
		final JSHintRunner runner = new JSHintRunner();
		runner.run(args);
	}

	private static String createShimCode() {
		// Create shims to prevent problems with JSHint accessing
		// objects that are not available in Rhino, e.g.
		// https://github.com/jshint/jshint/issues/1038
		return "console = {" //
				+ "log:function(){}," //
				+ "error:function(){}," //
				+ "trace:function(){}" //
				+ "};" //
				+ "window = {};" //
				+ "options = {};" //
				+ "global = this;";
	}

	private static Function findJSHintFunction(final ScriptableObject scope)
			throws IllegalArgumentException {
		if (!ScriptableObject.hasProperty(scope, "JSHINT")) {
			throw new IllegalArgumentException(
					"Global JSHINT function missing in input");
		}

		final Object object = scope.get("JSHINT", scope);
		if (!(object instanceof Function)) {
			throw new IllegalArgumentException(
					"Global JSHINT is not a function");
		}
		return (Function) object;
	}

	private static BufferedReader getJsHintReader() throws IOException {
		// Include DEFAULT_JSHINT_VERSION in name to ensure the constant matches
		// the actual version
		final String name = String.format("com/jshint/jshint-%s.js",
				DEFAULT_JSHINT_VERSION);

		final ClassLoader classLoader = JSHint.class.getClassLoader();
		final InputStream stream = classLoader.getResourceAsStream(name);
		return new BufferedReader(new InputStreamReader(stream, "UTF-8"));
	}

	private static int getPropertyAsInt(final ScriptableObject object,
			final String name, final int defaultValue) {
		final Object property = ScriptableObject.getProperty(object, name);
		if (property instanceof Number) {
			return ((Number) property).intValue();
		}
		return defaultValue;
	}

	private static String getPropertyAsString(final ScriptableObject object,
			final String name, final String defaultValue) {
		final Object property = ScriptableObject.getProperty(object, name);
		if (property instanceof CharSequence) {
			return ((CharSequence) property).toString();
		}
		return defaultValue;
	}

	private ScriptableObject scope;

	private Function jshint;

	private Object options;

	private Object globals;

	private int indent = DEFAULT_JSHINT_INDENT;

	/**
	 * Creates a new instance.
	 */
	public JSHint() {
	}

	/**
	 * Checks the given JavaScript code. All problems will be reported to the
	 * given problem handler.
	 *
	 * @param code
	 *            the JavaScript code to check, must not be null
	 * @param handler
	 *            the handler to report problems to or <code>null</code>
	 * @return <code>true</code> if no problems have been found, otherwise
	 *         <code>false</code>
	 */
	public boolean check(final String code, final ProblemHandler handler) {
		if (code == null) {
			throw new NullPointerException("code is null");
		}
		return check(new Text(code), handler);
	}

	public boolean check(final Text text, final ProblemHandler handler) {
		if (text == null) {
			throw new NullPointerException("code is null");
		}
		if (jshint == null) {
			throw new IllegalStateException("JSHint is not loaded");
		}
		boolean result = true;
		final String code = text.getContent();
		// Don't feed jshint with empty strings, see
		// https://github.com/jshint/jshint/issues/615
		// However, consider an empty string valid
		if (!code.trim().isEmpty()) {
			final Context context = Context.enter();
			try {
				result = checkCode(context, code);
				if (!result && handler != null) {
					handleProblems(handler, text);
				}

				// jshint.call(context, scope, null, args))
				// .booleanValue();
				// jshint.call(context, scope, thisObj, args)
				// jshint.get("data", jshint);

			} finally {
				Context.exit();
			}
		}
		return result;
	}

	/**
	 * Sets the configuration to use for all subsequent checks.
	 *
	 * @param configuration
	 *            the configuration to use, must not be null
	 */
	public void configure(final JsonObject configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration is null");
		}
		final Context context = Context.enter();
		try {
			final ScriptableObject scope = context.initStandardObjects();
			final JsonValue globalsValue = configuration.get("globals");
			if (globalsValue != null) {
				final String globalsExpression = String.format("globals = %s;",
						globalsValue);
				globals = context.evaluateString(scope, globalsExpression,
						"[globals]", 1, null);
			}
			configuration.remove("globals");
			final String optionsExpression = String.format("options = %s;",
					configuration);
			options = context.evaluateString(scope, optionsExpression,
					"[options]", 1, null);
			indent = determineIndent(configuration);
		} finally {
			Context.exit();
		}
	}

	/**
	 * Loads the default JSHint library.
	 *
	 * @see #getDefaultLibraryVersion()
	 */
	public void load() throws IOException {
		try (final Reader reader = getJsHintReader()) {
			load(reader);
		}
	}

	/**
	 * Loads a custom JSHint library. The input stream must provide the contents
	 * of the file <code>jshint.js</code> found in the JSHint distribution.
	 *
	 * @param stream
	 *            an input stream to load the the JSHint library from.
	 * @throws IOException
	 *             if an I/O error occurs while reading from the input stream.
	 * @throws IllegalArgumentException
	 *             if the given input is not a proper JSHint library file.
	 */
	public void load(final InputStream stream) throws IOException {
		try (final Reader reader = new InputStreamReader(stream)) {
			load(reader);
		}
	}

	private boolean checkCode(final Context context, final String code) {
		try {
			final Object[] args = new Object[] { code, options, globals };
			return ((Boolean) jshint.call(context, scope, null, args))
					.booleanValue();
		} catch (final JavaScriptException e) {
			final String message = "JavaScript exception thrown by JSHint: "
					+ e.getMessage();
			throw new RuntimeException(message, e);
		} catch (final RhinoException e) {
			final String message = "JavaScript exception caused by JSHint: "
					+ e.getMessage();
			throw new RuntimeException(message, e);
		}
	}

	private int determineIndent(final JsonObject configuration) {
		final JsonValue value = configuration.get("indent");
		if (value != null && value.isNumber()) {
			return value.asInt();
		}
		return DEFAULT_JSHINT_INDENT;
	}

	// private void findOptions(final Context context,
	// final ScriptableObject scope) {
	//
	// System.out.println(jshint.has("data", jshint));
	// final Object o = jshint.get("data", jshint);
	// System.out.println(o);
	//
	// final Object object = jshint.get("boolOptions", jshint);
	// if (object instanceof Scriptable) {
	// final Scriptable s = (Scriptable) object;
	// final Object[] ids = s.getIds();
	// for (final Object id : ids) {
	// System.out.println(id);
	// }
	// System.out.println();
	// }
	// // if (object instanceof JsnObject) {
	// // System.out.println(object);
	// // }
	// // JSObject rick =
	// // rick.getMember("name")
	// final Scriptable obj = (Scriptable) jshint.get("data", jshint);
	// final Object dd = obj.get("options", obj);
	// System.out.println(obj);
	// System.out.println(dd);
	//
	// Object[] ids = jshint.getIds();
	// for (final Object id : ids) {
	// System.out.println(id);
	// }
	//
	// ids = ScriptableObject.getPropertyIds(scope);
	// for (final Object id : ids) {
	// System.out.println(id);
	// }
	// }

	private void handleProblems(final ProblemHandler handler, final Text text) {
		final NativeArray errors = (NativeArray) jshint.get("errors", jshint);
		final long length = errors.getLength();
		for (int i = 0; i < length; i++) {
			final Object object = errors.get(i, errors);
			final ScriptableObject error = (ScriptableObject) object;
			if (error != null) {
				final Problem problem = createProblem(error, text);
				handler.handleProblem(problem);
			}
		}
	}

	private void load(final Reader reader) throws IOException {
		final Context context = Context.enter();
		try {
			context.setOptimizationLevel(9);
			context.setLanguageVersion(Context.VERSION_1_5);
			scope = context.initStandardObjects();
			context.evaluateString(scope, createShimCode(), "shim", 1, null);
			context.evaluateReader(scope, reader, "jshint-library", 1, null);
			jshint = findJSHintFunction(scope);

			// findOptions(context, scope);

		} catch (final RhinoException e) {
			throw new IOException("Could not evaluate JavaScript input.", e);
		} finally {
			Context.exit();
		}
	}

	// private List<String> readBooleans(final NativeObject exports,
	// final String key) {
	// try {
	// System.out.println(exports.get("bool"));
	// final NativeObject bool = (NativeObject) exports.get("bool");
	// final NativeObject map = (NativeObject) bool.get(key);
	// return readMap(map);
	// } catch (final Exception e) {
	// return Collections.emptyList();
	// }
	// }

	// private List<String> readMap(final NativeObject source) {
	// final List<String> result = new ArrayList<>(source.size());
	// for (final Object key : source.keySet()) {
	// result.add(key.toString());
	// }
	// return result;
	// }

	// private List<String> readValues(final NativeObject exports,
	// final String key) {
	// try {
	// final NativeObject map = (NativeObject) exports.get("val");
	// return readMap(map);
	// } catch (final Exception e) {
	// return Collections.emptyList();
	// }
	// }

	/**
	 * See: http://jshint.com/docs/reporters/
	 *
	 * <pre>
	 * {
	 *      file:  [string, filename]
	 *      error: {
	 *              id:        [string, usually '(error)'],
	 *              code:      [string, error/warning code],
	 *              reason:    [string, error/warning message],
	 *              evidence:  [string, a piece of code that generated this error]
	 *              line:      [number]
	 *              character: [number]
	 *              scope:     [string, message scope; usually '(main)' unless the code was eval'ed]
	 *              [+ a few other legacy fields that you don't need to worry about.]
	 *      }
	 * }
	 * </pre>
	 */
	Problem createProblem(final ScriptableObject error, final Text text) {
		final String reason = getPropertyAsString(error, "reason", "");
		int line = getPropertyAsInt(error, "line", -1);
		int character = getPropertyAsInt(error, "character", -1);
		final String code = getPropertyAsString(error, "code", "");

		if (line <= 0 || line > text.getLineCount()) {
			line = -1;
			character = -1;
		} else if (character > 0) {
			character = visualToCharIndex(text, line, character);
		}

		return new ProblemImpl(line, character, reason, code);
	}

	/**
	 * JSHint reports "visual" character positions instead of a character index,
	 * i.e. the first character is 1 and every tab character is multiplied by
	 * the indent with. Example with tabulation as 4 spaces:
	 * <p>
	 * <code>
	 * <pre>
	 *          "a\tb\tc"
	 *
	 * index:  | 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10|
	 * char:   | a | » | b | » | c |
	 * visual:     | a | »             | b | »             | c |
	 * </pre>
	 * </code>
	 * </p>
	 */
	int visualToCharIndex(final Text text, final int line,
			final int character) {
		boolean isTab;
		int charIndex = 0;
		int visualIndex = 1;
		final String string = text.getContent();
		final int offset = text.getLineOffset(line - 1);
		final int maxCharIndex = string.length() - offset - 1;

		while (visualIndex != character && charIndex < maxCharIndex) {
			isTab = string.charAt(offset + charIndex) == '\t';
			visualIndex += isTab ? indent : 1;
			charIndex++;
		}
		return charIndex;
	}
}
