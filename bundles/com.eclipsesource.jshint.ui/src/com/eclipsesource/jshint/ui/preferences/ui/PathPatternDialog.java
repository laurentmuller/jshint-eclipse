/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ralf Sternberg initial implementation and API
 ******************************************************************************/
package com.eclipsesource.jshint.ui.preferences.ui;

import static com.eclipsesource.jshint.ui.util.LayoutUtil.gridData;
import static com.eclipsesource.jshint.ui.util.LayoutUtil.gridLayout;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.eclipsesource.jshint.ui.preferences.PathPattern;
import com.eclipsesource.jshint.ui.preferences.PathSegmentPattern;

public class PathPatternDialog extends TitleAreaDialog {

	private static String checkFilePattern(final String pattern) {
		try {
			PathSegmentPattern.create(pattern);
		} catch (final IllegalArgumentException exception) {
			return exception.getMessage().replace("in expression",
					"in file pattern");
		}
		return null;
	}

	private static String checkFolderPattern(final String pattern) {
		if (pattern.contains("//")) {
			return "Illegal '//' in folder path";
		} else {
			try {
				PathPattern.create(pattern);
			} catch (final IllegalArgumentException exception) {
				return exception.getMessage().replace("in expression",
						"in folder path");
			}
		}
		return null;
	}

	private static String trimSlashes(final String string) {
		String result = string;
		while (result.startsWith("/")) {
			result = result.substring(1);
		}
		while (result.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	private Button allFoldersRadiobox;
	private Button selectedFolderRadiobox;
	private Button selectFolderButton;

	private Button includeSubFoldersCheckbox;
	private Text folderPatternText;

	private Button allFilesRadiobox;
	private Button matchingFilesRadiobox;
	private Text filePatternText;

	private String value;
	private String title;
	private final PathPattern pattern;
	private final IProject project;

	public PathPatternDialog(final Shell parent, final String pattern,
			final IProject project) {
		super(parent);
		setHelpAvailable(false);
		this.pattern = pattern == null ? null : PathPattern.create(pattern);
		this.project = project;
	}

	PathPatternDialog(final Shell parent, final String pattern) {
		this(parent, pattern, null);
	}

	public String getValue() {
		return value;
	}

	@Override
	public void setErrorMessage(final String newErrorMessage) {
		super.setErrorMessage(newErrorMessage);
		setOkEnabled(newErrorMessage == null);
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
		super.setTitle(title);
	}

	private void addFileRadioListeners() {
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				updateFileDetailsEnablementFromSelection();
				validate();
			}
		};
		allFilesRadiobox.addListener(SWT.Selection, listener);
		matchingFilesRadiobox.addListener(SWT.Selection, listener);
	}

	private void addFileTextListener() {
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				validate();
			}
		};
		filePatternText.addListener(SWT.Modify, listener);
	}

	private void addFolderRadioListeners() {
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				updateFolderDetailsEnablementFromSelection();
				validate();
			}
		};
		allFoldersRadiobox.addListener(SWT.Selection, listener);
		selectedFolderRadiobox.addListener(SWT.Selection, listener);
	}

	private void addFolderTextListener() {
		final Listener listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				validate();
			}
		};
		folderPatternText.addListener(SWT.Modify, listener);
	}

	private Button createCheckBox(final Composite parent, final String text) {
		final Button widget = new Button(parent, SWT.CHECK);
		if (text != null) {
			widget.setText(text);
		}
		return widget;
	}

	private Control createFileArea(final Composite parent) {
		final Group group = createGroup(parent, "Files");
		createFileAreaControls(group);
		addFileRadioListeners();
		addFileTextListener();
		return group;
	}

	private void createFileAreaControls(final Composite parent) {
		allFilesRadiobox = createOptionBox(parent, "A&ll files");
		matchingFilesRadiobox = createOptionBox(parent, "F&ile name patterns");

		filePatternText = new Text(parent, SWT.BORDER);
		gridData(filePatternText).fillHorizontal().indent(20, 0);

		final Label label = new Label(parent, SWT.NONE);
		label.setText("(* = any string, ? = any character)");
		gridData(label).indent(20, 0);
	}

	private Control createFolderArea(final Composite parent) {
		final Group group = createGroup(parent, "Folders");
		createFolderAreaControls(group);
		addFolderRadioListeners();
		addFolderTextListener();
		return group;
	}

	private void createFolderAreaControls(final Composite parent) {
		allFoldersRadiobox = createOptionBox(parent, "&All folders");
		selectedFolderRadiobox = createOptionBox(parent,
				"&Folder path (empty = project root folder)");

		final Composite folderContainer = new Composite(parent, SWT.NONE);
		gridData(folderContainer).fillHorizontal().indent(20, 0);
		gridLayout(folderContainer).columns(2).spacing(5);

		folderPatternText = new Text(folderContainer, SWT.BORDER);
		gridData(folderPatternText).fillHorizontal();

		selectFolderButton = new Button(folderContainer, SWT.PUSH);
		selectFolderButton.setText(JFaceResources.getString("openBrowse"));
		selectFolderButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				selectContainer();
			}
		});
		includeSubFoldersCheckbox = createCheckBox(parent,
				"Include &subfolders");
		gridData(includeSubFoldersCheckbox).indent(20, 0);

	}

	private Group createGroup(final Composite parent, final String text) {
		final Group group = new Group(parent, SWT.NONE);
		if (text != null) {
			group.setText(text);
		}
		gridLayout(group).spacing(5).margin(10);
		return group;
	}

	private Button createOptionBox(final Composite parent, final String text) {
		final Button widget = new Button(parent, SWT.RADIO);
		if (text != null) {
			widget.setText(text);
		}
		return widget;
	}

	private void createPatternFromUI() {
		final StringBuilder builder = new StringBuilder();
		if (allFoldersRadiobox.getSelection()) {
			builder.append("//");
		} else {
			final String folderPattern = folderPatternText.getText().trim();
			builder.append(trimSlashes(folderPattern));
			if (includeSubFoldersCheckbox.getSelection()) {
				builder.append("//");
			} else if (folderPattern.length() > 0) {
				builder.append("/");
			}
		}
		if (allFilesRadiobox.getSelection()) {
			builder.append("*");
		} else {
			builder.append(filePatternText.getText().trim());
		}
		value = builder.toString();
	}

	private String getCombinedErrorMessage() {
		final String folderError = getFolderErrorMessage();
		final String fileError = getFileErrorMessage();

		if (fileError == null) {
			return folderError;
		} else if (folderError == null) {
			return fileError;
		} else {
			return folderError + ", " + fileError;
		}
	}

	private String getFileErrorMessage() {
		if (matchingFilesRadiobox.getSelection()) {
			final String text = filePatternText.getText();
			return checkFilePattern(text);
		}
		return null;
	}

	private String getFolderErrorMessage() {
		if (selectedFolderRadiobox.getSelection()) {
			final String text = folderPatternText.getText();
			return checkFolderPattern(text);
		}
		return null;
	}

	private IResource getSelectedContainer() {
		final String path = folderPatternText.getText().trim();
		if (path.isEmpty()) {
			return null;
		}
		if (!Path.EMPTY.isValidPath(path)) {
			return null;
		}
		return project.findMember(path, false);
	}

	private void initializeUI() {
		if (pattern == null || pattern.matchesAllFiles()) {
			allFilesRadiobox.setSelection(true);
		} else {
			matchingFilesRadiobox.setSelection(true);
			filePatternText.setText(pattern.getFilePattern());
		}
		if (pattern == null || pattern.matchesAllFolders()) {
			allFoldersRadiobox.setSelection(true);
			includeSubFoldersCheckbox.setSelection(false);
		} else {
			selectedFolderRadiobox.setSelection(true);
			final String folderPattern = pattern.getPathPattern();
			folderPatternText.setText(folderPattern.replace("//", "/"));
			includeSubFoldersCheckbox
					.setSelection(folderPattern.endsWith("//"));
		}
		updateFileDetailsEnablementFromSelection();
		updateFolderDetailsEnablementFromSelection();
		super.setTitle(title);
	}

	private void selectContainer() {
		final ContainerSelectionDialog dlg = new ContainerSelectionDialog(
				getShell(), project);
		dlg.setInitialSelection(getSelectedContainer());

		if (dlg.open() == Window.OK) {
			final IContainer container = dlg.getFirstResult();
			final String text = container.getProjectRelativePath()
					.toPortableString();
			folderPatternText.setText(text);
			folderPatternText.selectAll();
			folderPatternText.setFocus();
		}
	}

	private void setOkEnabled(final boolean enabled) {
		final Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) {
			okButton.setEnabled(enabled);
		}
	}

	private void updateFileDetailsEnablementFromSelection() {
		filePatternText.setEnabled(matchingFilesRadiobox.getSelection());
	}

	private void updateFolderDetailsEnablementFromSelection() {
		folderPatternText.setEnabled(selectedFolderRadiobox.getSelection());
		selectFolderButton.setEnabled(selectedFolderRadiobox.getSelection());
		includeSubFoldersCheckbox
				.setEnabled(selectedFolderRadiobox.getSelection());
	}

	private void validate() {
		final String errorMessage = getCombinedErrorMessage();
		setErrorMessage(errorMessage);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText(
				pattern != null ? "Edit path pattern" : "New path pattern");
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Control composite = super.createDialogArea(parent);
		final Composite contentArea = new Composite(parent, SWT.NONE);
		gridLayout(contentArea).margin(10).spacing(10);
		gridData(contentArea).fillBoth();

		final Control folderArea = createFolderArea(contentArea);
		gridData(folderArea).fillBoth();

		final Control fileArea = createFileArea(contentArea);
		gridData(fileArea).fillBoth();

		initializeUI();
		return composite;
	}

	@Override
	protected void okPressed() {
		createPatternFromUI();
		super.okPressed();
	}

	String getTitle() {
		return title;
	}
}
