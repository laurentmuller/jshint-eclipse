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
package com.eclipsesource.jshint.ui.preferences.ui;

import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridData;
import static com.eclipsesource.jshint.ui.util.LayoutUtils.gridLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import com.eclipsesource.jshint.ui.preferences.EnablementPreferences;

public class IncludesView extends Composite {

	private static List<String> getPatterns(final Table table) {
		final TableItem[] items = table.getItems();
		final List<String> result = new ArrayList<String>();
		for (final TableItem item : items) {
			result.add(item.getText());
		}
		return result;
	}

	private static void select(final Table table, final int index) {
		final int count = table.getItemCount();
		if (index >= 0 && index < count) {
			table.select(index);
		} else if (count > 0) {
			table.select(count - 1);
		}
	}

	private static void select(final Table table, final String pattern) {
		final TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (items[i].getText().equals(pattern)) {
				table.select(i);
			}
		}
	}

	private final Table includeTable;

	private final Table excludeTable;

	private final Image fileImage;

	private final IProject project;

	public IncludesView(final Composite parent, final int style,
			final IProject project) {
		super(parent, style);
		gridLayout(this).columns(2).spacing(5, 3);

		fileImage = createImage();
		includeTable = createTable(
				"Enable JSHint for these files and folders:");
		excludeTable = createTable(
				"Exclude these files and folders from validation:");

		this.project = project;
	}

	public void loadDefaults() {
		setPatterns(includeTable, Collections.<String> emptyList());
		setPatterns(excludeTable, Collections.<String> emptyList());
	}

	public void loadPreferences(final EnablementPreferences preferences) {
		final List<String> includePatterns = preferences.getIncludePatterns();
		final List<String> excludePatterns = preferences.getExcludePatterns();
		setPatterns(includeTable, includePatterns);
		setPatterns(excludeTable, excludePatterns);
	}

	public void storePreferences(final EnablementPreferences preferences) {
		final List<String> includePatterns = getPatterns(includeTable);
		final List<String> excludePatterns = getPatterns(excludeTable);
		preferences.setIncludePatterns(includePatterns);
		preferences.setExcludePatterns(excludePatterns);
	}

	private void addListeners(final Table table) {
		table.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editPattern(table);
			}
		});
		table.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.detail == SWT.TRAVERSE_RETURN) {
					event.doit = false;
				}
			}
		});
	}

	private void addPattern(final Table table) {
		final String defaultPattern = table == includeTable ? "//*.js"
				: "//*.min.js";
		final String pattern = showPatternDialogForTable(table, defaultPattern);
		if (pattern != null) {
			final List<String> patterns = getPatterns(table);
			if (!patterns.contains(pattern)) {
				patterns.add(pattern);
				setPatterns(table, patterns);
				select(table, pattern);
			}
		}
	}

	private void configurePatternDialog(final PathPatternDialog dialog,
			final Table table) {
		if (excludeTable.equals(table)) {
			dialog.setTitle("Select folders and files to exclude");
		} else {
			dialog.setTitle("Select folders and files to include");
		}
	}

	private void createAddButton(final Table table, final ButtonBar buttonBar) {
		buttonBar.addButton("Add", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				addPattern(table);
			}
		});
	}

	private void createButtonsBar(final Table table) {
		final ButtonBar buttonBar = new ButtonBar(this, SWT.NONE);
		gridData(buttonBar).fillVertical();

		createAddButton(table, buttonBar);
		createEditButton(table, buttonBar);
		createRemoveButton(table, buttonBar);
	}

	private void createEditButton(final Table table,
			final ButtonBar buttonBar) {
		final Button button = buttonBar.addButton("Edit", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				editPattern(table);
			}
		});
		button.setEnabled(false);
		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				button.setEnabled(table.getSelectionCount() > 0);
			}
		});
	}

	private Image createImage() {
		final ISharedImages images = PlatformUI.getWorkbench()
				.getSharedImages();
		return images.getImage(ISharedImages.IMG_OBJ_FILE);
	}

	private void createRemoveButton(final Table table,
			final ButtonBar buttonBar) {
		final Button button = buttonBar.addButton("Remove", new Listener() {
			@Override
			public void handleEvent(final Event event) {
				removeSelectedPattern(table);
			}
		});
		button.setEnabled(false);
		table.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				button.setEnabled(table.getSelectionCount() > 0);
			}
		});
	}

	private Table createTable(final String title) {
		final Label label = new Label(this, SWT.NONE);
		label.setText(title);
		gridData(label).span(2, 1);

		final Table table = new Table(this, SWT.BORDER);
		gridData(table).fillBoth();

		createButtonsBar(table);
		addListeners(table);

		return table;
	}

	private void editPattern(final Table table) {
		final TableItem item = getSelection(table);
		if (item != null) {
			final String oldPattern = item.getText();
			final String newPattern = showPatternDialogForTable(table,
					oldPattern);
			if (newPattern != null && !newPattern.equals(oldPattern)) {
				final List<String> patterns = getPatterns(table);
				patterns.remove(oldPattern);
				patterns.add(newPattern);
				setPatterns(table, patterns);
				select(table, newPattern);
			}
		}
	}

	private TableItem getSelection(final Table table) {
		final int selection = table.getSelectionIndex();
		if (selection != -1) {
			return table.getItem(selection);
		}
		return null;
	}

	private void removeSelectedPattern(final Table table) {
		final TableItem item = getSelection(table);
		if (item != null) {
			final int index = table.indexOf(item);
			final String pattern = item.getText();
			final List<String> patterns = getPatterns(table);
			patterns.remove(pattern);
			setPatterns(table, patterns);
			select(table, index);
		}
	}

	private void setPatterns(final Table table, final List<String> patterns) {
		Collections.sort(patterns);
		table.removeAll();
		for (final String pattern : patterns) {
			final TableItem item = new TableItem(table, SWT.NONE);
			item.setImage(fileImage);
			item.setText(pattern);
		}
	}

	private String showPatternDialogForTable(final Table table,
			final String pattern) {
		final PathPatternDialog dlg = new PathPatternDialog(getShell(), pattern,
				project);
		configurePatternDialog(dlg, table);
		if (dlg.open() == Window.OK) {
			return dlg.getValue();
		}
		return null;
	}
}
