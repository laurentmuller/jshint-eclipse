/*******************************************************************************
 * Copyright (c) 2017 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Laurent Muller - initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences.ui;

import java.io.File;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.eclipsesource.jshint.ui.Activator;
import com.eclipsesource.jshint.ui.options.JSHintOption;
import com.eclipsesource.jshint.ui.util.IOUtil;
import com.eclipsesource.jshint.ui.util.JsonUtil;
import com.eclipsesource.json.ParseException;

public class ConfigurationEditor extends StyledText {

	private static final StyleRange[] EMPTY_STYLES = {};

	private static final char QUOTE_CHAR = '"';

	private static Color getErrorColor() {
		return JFaceResources.getColorRegistry()
				.get(JFacePreferences.ERROR_COLOR);
	}

	private final StyleRange[] errorStyles;

	public ConfigurationEditor(final Composite parent) {
		super(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		setFont(JFaceResources.getTextFont());

		addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				validate();
			}
		});
		addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				switch (e.detail) {
				case SWT.TRAVERSE_TAB_NEXT:
				case SWT.TRAVERSE_TAB_PREVIOUS:
					e.doit = true;
					break;
				}
			}
		});

		// error styles
		final StyleRange style = new StyleRange();
		style.length = 1;
		style.underline = true;
		style.underlineColor = getErrorColor();
		style.underlineStyle = SWT.UNDERLINE_ERROR;
		errorStyles = new StyleRange[] { style };
	}

	// public void addOption(final String option) {
	// addOption(option, null);
	// }

	public void addOption(final JSHintOption o) {
		// option?
		if (o == null) {
			return;
		}

		// name and value
		final String name = quote(o.getName());
		final String value = o.getValue().toString();

		// already present?
		final String text = getText();
		if (text.contains(name)) {
			return;
		}

		// find insertion index
		int index = -1;
		String indent = "";
		boolean found = false;
		for (int i = 0; i < text.length(); i++) {
			switch (text.charAt(i)) {
			case '{':
			case '\r':
			case '\n':
				index = i + 1;
				break;
			case ' ':
				indent += ' ';
				break;
			case '\t':
				indent += '\t';
				break;
			default:
				found = true;
				break;
			}
			if (found) {
				break;
			}
		}
		index = Math.max(0, index);

		// new line if in first position
		if (index == 1 && text.charAt(0) == '{') {
			replaceTextRange(index, 0, "\n");
			index++;
		}

		// indent
		replaceTextRange(index, 0, indent);
		index += indent.length();

		// name
		replaceTextRange(index, 0, name);
		index += name.length();

		// separator
		replaceTextRange(index, 0, ": ");
		index += 2;

		// value
		replaceTextRange(index, 0, value);

		// select
		setSelection(index, index + value.length());
		index += value.length();

		// separator
		if (index < text.length() - 1) {
			replaceTextRange(index, 0, ",");
			index++;
		}

		// new line
		replaceTextRange(index, 0, "\n");

		// focus
		setFocus();
	}

	public void exportConfig() {
		final File file = selectFile(SWT.SAVE);
		if (file != null) {
			try {
				final String json = JsonUtil.prettyPrint(getText());
				IOUtil.writeUtf8File(file, json);
			} catch (final Exception e) {
				final String msg = String
						.format("Could not write to file '%s'.", file);
				MessageDialog.openError(getShell(), "Export Failed",
						msg + "\nSee log for details.");
				Activator.handleError(msg, e);
			}
		}
	}

	public void formatContent() {
		try {
			final String text = JsonUtil.prettyPrint(getText());
			setText(text);
		} catch (final Exception e) {
			// ignore
		}
	}

	public void importConfig() {
		final File file = selectFile(SWT.OPEN);
		if (file != null) {
			try {
				final String text = IOUtil.readUtf8File(file);
				final String json = JsonUtil.prettyPrint(text);
				setText(json);
			} catch (final Exception e) {
				final String msg = String
						.format("Could not read from file '%s'.", file);
				MessageDialog.openError(getShell(), "Import Failed",
						msg + "\nSee log for details.");
				Activator.handleError(msg, e);
			}
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		if (enabled) {
			setBackground(null);
		} else {
			setBackground(
					getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			removeErrorMarker();
		}
	}

	public void validate() {
		try {
			JsonUtil.readFrom(getText());
			removeErrorMarker();
			handleError(null);

		} catch (final ParseException e) {
			final int line = e.getLine() - 1;
			final int column = e.getColumn();
			setErrorMarker(line, column);
			handleError(String.format("Syntax error at line: %d, column: %d.",
					line, column));
		}
	}

	/**
	 * Handles the error. Subclass can override this function to take
	 * appropriate action.
	 *
	 * @param message
	 *            the error message or <code>null</code> if no error.
	 */
	protected void handleError(final String message) {
		// NO-OP: Used by subclass
	}

	private int checkRange(final int value, final int max) {
		if (value < 0) {
			return 0;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}

	private String quote(String str) {
		if (str.isEmpty() || str.charAt(0) != QUOTE_CHAR) {
			str = QUOTE_CHAR + str;
		}
		if (str.length() == 1 || str.charAt(str.length() - 1) != QUOTE_CHAR) {
			str = str + QUOTE_CHAR;
		}
		return str;
	}

	private void removeErrorMarker() {
		setStyleRanges(EMPTY_STYLES);
	}

	private File selectFile(final int style) {
		final Shell shell = getShell();
		final FileDialog dialog = new FileDialog(shell, style);
		dialog.setOverwrite(true);
		final String fileName = dialog.open();
		if (fileName != null) {
			return new File(fileName);
		}
		return null;
	}

	private void setErrorMarker(final int line, final int column) {
		final int lineIndex = checkRange(getLineCount() - 1, line);
		int offset = getOffsetAtLine(lineIndex);
		final String lineText = getLine(lineIndex);
		offset += checkRange(lineText.length() - 1, column);
		errorStyles[0].start = offset;

		try {
			setStyleRanges(errorStyles);
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
			setStyleRanges(EMPTY_STYLES);
		}
	}
}
